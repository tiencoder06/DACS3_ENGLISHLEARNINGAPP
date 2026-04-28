package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.PlacementQuestion
import com.example.englishlearningapp.data.model.PlacementResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val topicRepository: TopicRepository,
    private val lessonRepository: LessonRepository
) : PlacementRepository {

    override fun getPlacementQuestions(): Flow<Result<List<PlacementQuestion>>> = callbackFlow {
        val subscription = firestore.collection("placement_questions")
            .whereEqualTo("status", "active")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PlacementRepo", "Firestore error: ${error.message}")
                    trySend(Result.success(getFallbackQuestions()))
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val questions = snapshot.toObjects(PlacementQuestion::class.java)
                    trySend(Result.success(questions))
                } else {
                    Log.d("PlacementRepo", "No questions in Firestore, using fallback")
                    trySend(Result.success(getFallbackQuestions()))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun calculatePlacementResult(
        questions: List<PlacementQuestion>,
        userAnswers: Map<String, String>
    ): PlacementResult {
        var correctCount = 0
        val sectionTotals = mutableMapOf<String, Int>()
        val sectionCorrects = mutableMapOf<String, Int>()

        questions.forEach { question ->
            val section = question.section
            sectionTotals[section] = sectionTotals.getOrDefault(section, 0) + 1
            
            val userAnswer = userAnswers[question.questionId]
            if (userAnswer != null && userAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
                correctCount++
                sectionCorrects[section] = sectionCorrects.getOrDefault(section, 0) + 1
            }
        }

        val totalQuestions = questions.size
        val scorePercentage = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

        val level = when {
            scorePercentage <= 30 -> "Beginner"
            scorePercentage <= 55 -> "Elementary"
            scorePercentage <= 75 -> "Pre-Intermediate"
            else -> "Intermediate"
        }

        // Calculate Skills
        val sectionPercentages = sectionTotals.mapValues { (section, total) ->
            (sectionCorrects.getOrDefault(section, 0) * 100) / total
        }

        val strongSkill: String
        val weakSkill: String

        if (sectionPercentages.isEmpty()) {
            strongSkill = ""
            weakSkill = ""
        } else {
            val maxEntry = sectionPercentages.maxByOrNull { it.value }
            val minEntry = sectionPercentages.minByOrNull { it.value }

            val maxVal = maxEntry?.value ?: 0
            val minVal = minEntry?.value ?: 0

            if (maxVal == minVal) {
                strongSkill = "Balanced"
                weakSkill = "Balanced"
            } else {
                strongSkill = maxEntry?.key ?: ""
                weakSkill = minEntry?.key ?: ""
            }
        }

        val recommendedContent = getRecommendedStartContent(level)

        return PlacementResult(
            score = scorePercentage,
            level = level,
            strongSkill = strongSkill,
            weakSkill = weakSkill,
            recommendedTopicId = recommendedContent.first,
            recommendedLessonId = recommendedContent.second,
            sectionScores = sectionPercentages
        )
    }

    override suspend fun savePlacementResult(
        userId: String,
        result: PlacementResult
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "placementCompleted" to true,
                "placementSkipped" to false,
                "placementScore" to result.score,
                "placementLevel" to result.level,
                "placementStrongSkill" to result.strongSkill,
                "placementWeakSkill" to result.weakSkill,
                "recommendedStartTopicId" to result.recommendedTopicId,
                "recommendedStartLessonId" to result.recommendedLessonId,
                "placementTakenAt" to Timestamp.now()
                // Do NOT overwrite user's main level automatically unless business confirms, 
                // but user specified "placementLevel should be separate from profile level".
                // However, user's previous instruction mentioned syncing, but latest says "Do NOT overwrite".
                // Let's stick to separate fields for now as per "placementLevel should be separate".
            )
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun skipPlacement(userId: String): Result<Unit> {
        return try {
            val fallbackContent = getRecommendedStartContent("Beginner")
            val updates = mapOf(
                "placementCompleted" to true,
                "placementSkipped" to true,
                "placementScore" to 0,
                "placementLevel" to "Beginner",
                "placementStrongSkill" to "",
                "placementWeakSkill" to "",
                "recommendedStartTopicId" to fallbackContent.first,
                "recommendedStartLessonId" to fallbackContent.second,
                "placementTakenAt" to Timestamp.now()
            )
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedStartContent(level: String): Pair<String, String> {
        return try {
            val topics = topicRepository.getTopics().filter { it.status == "active" }
            if (topics.isEmpty()) return Pair("", "")

            val targetTopic = when (level) {
                "Beginner" -> topics.first()
                "Elementary" -> topics[(topics.size * 0.25).toInt().coerceAtMost(topics.size - 1)]
                "Pre-Intermediate" -> topics[(topics.size * 0.5).toInt().coerceAtMost(topics.size - 1)]
                "Intermediate" -> topics[(topics.size * 0.75).toInt().coerceAtMost(topics.size - 1)]
                else -> topics.first()
            }

            val lessons = lessonRepository.getLessons(targetTopic.topicId).filter { it.status == "active" }
            val lessonId = if (lessons.isNotEmpty()) lessons.first().lessonId else ""

            Pair(targetTopic.topicId, lessonId)
        } catch (e: Exception) {
            Log.e("PlacementRepo", "Error getting recommended content: ${e.message}")
            Pair("", "")
        }
    }

    private fun getFallbackQuestions(): List<PlacementQuestion> {
        return listOf(
            PlacementQuestion(
                questionId = "fallback_1",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "I ___ to the gym every morning.",
                options = listOf("go", "goes", "going", "gone"),
                correctAnswer = "go",
                order = 1,
                level = 1
            ),
            PlacementQuestion(
                questionId = "fallback_2",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "She ___ English for three years.",
                options = listOf("studies", "is studying", "has been studying", "studied"),
                correctAnswer = "has been studying",
                order = 2,
                level = 2
            ),
            PlacementQuestion(
                questionId = "fallback_3",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "If I ___ you, I would take the job.",
                options = listOf("am", "was", "were", "be"),
                correctAnswer = "were",
                order = 3,
                level = 3
            ),
            PlacementQuestion(
                questionId = "fallback_4",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "That is the house ___ I was born.",
                options = listOf("which", "where", "who", "when"),
                correctAnswer = "where",
                order = 4,
                level = 2
            ),
            PlacementQuestion(
                questionId = "fallback_5",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "They ___ lunch when the phone rang.",
                options = listOf("ate", "were eating", "had eaten", "eat"),
                correctAnswer = "were eating",
                order = 5,
                level = 2
            ),
            PlacementQuestion(
                questionId = "fallback_6",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "He is very good ___ playing the piano.",
                options = listOf("at", "in", "on", "with"),
                correctAnswer = "at",
                order = 6,
                level = 1
            ),
            PlacementQuestion(
                questionId = "fallback_7",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "We don't have ___ milk left.",
                options = listOf("some", "any", "many", "a"),
                correctAnswer = "any",
                order = 7,
                level = 1
            ),
            PlacementQuestion(
                questionId = "fallback_8",
                section = "vocabulary_grammar",
                questionType = "multiple_choice",
                questionText = "I look forward to ___ you soon.",
                options = listOf("see", "seeing", "seen", "saw"),
                correctAnswer = "seeing",
                order = 8,
                level = 3
            ),
            PlacementQuestion(
                questionId = "fallback_9",
                section = "listening",
                questionType = "listen_choose",
                questionText = "What time is the meeting?",
                audioText = "The meeting starts at half past ten.",
                options = listOf("10:00", "10:30", "11:00", "11:30"),
                correctAnswer = "10:30",
                order = 9,
                level = 2
            ),
            PlacementQuestion(
                questionId = "fallback_10",
                section = "listening",
                questionType = "listen_choose",
                questionText = "Where is the post office?",
                audioText = "Go straight and turn left at the second corner.",
                options = listOf("First corner", "Second corner, left", "Third corner", "Opposite the bank"),
                correctAnswer = "Second corner, left",
                order = 10,
                level = 1
            ),
            PlacementQuestion(
                questionId = "fallback_11",
                section = "listening",
                questionType = "listen_choose",
                questionText = "Why was he late?",
                audioText = "He missed the bus because his alarm didn't go off.",
                options = listOf("Woke up late", "Bus was late", "Traffic jam", "Alarm clock broke"),
                correctAnswer = "Alarm clock broke",
                order = 11,
                level = 3
            ),
            PlacementQuestion(
                questionId = "fallback_12",
                section = "sentence_usage",
                questionType = "multiple_choice",
                questionText = "Customer: 'How much is this?' - Clerk: '___'",
                options = listOf("It is big.", "It's $15.", "I am fine.", "Thank you."),
                correctAnswer = "It's $15.",
                order = 12,
                level = 1
            ),
            PlacementQuestion(
                questionId = "fallback_13",
                section = "sentence_usage",
                questionType = "multiple_choice",
                questionText = "Person A: 'I've passed my driving test!' - Person B: '___'",
                options = listOf("Good luck!", "Congratulations!", "Never mind.", "I'm sorry."),
                correctAnswer = "Congratulations!",
                order = 13,
                level = 2
            )
        )
    }
}
