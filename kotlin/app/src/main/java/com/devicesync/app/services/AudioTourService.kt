package com.devicesync.app.services

import android.content.Context
import android.media.MediaPlayer
import com.devicesync.app.data.*
import kotlinx.coroutines.delay
import java.util.*

class AudioTourService(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentTour: AudioTour? = null
    private var currentStop: AudioStop? = null
    private var isPlaying = false
    private var currentPosition = 0
    
    private val audioTours = mutableListOf<AudioTour>()
    private val tourProgress = mutableMapOf<String, AudioTourProgress>()
    
    init {
        initializeSampleAudioTours()
    }
    
    private fun initializeSampleAudioTours() {
        // Burj Khalifa Audio Tour
        val burjKhalifaTour = AudioTour(
            id = "burj_khalifa_tour",
            title = "Burj Khalifa: Towering Above Dubai",
            description = "Discover the world's tallest building with this comprehensive audio guide",
            location = "Burj Khalifa, Downtown Dubai",
            duration = 45,
            language = "English",
            audioUrl = "https://example.com/audio/burj_khalifa_tour.mp3",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&h=600&fit=crop&crop=center",
            stops = listOf(
                AudioStop(
                    id = "stop_1",
                    title = "Welcome to Burj Khalifa",
                    description = "Introduction to the world's tallest building",
                    coordinates = Coordinates(25.197197, 55.274376),
                    audioUrl = "https://example.com/audio/burj_welcome.mp3",
                    duration = 180,
                    facts = listOf(
                        "Burj Khalifa stands 828 meters tall",
                        "It has 163 floors",
                        "Construction took 6 years to complete"
                    ),
                    tips = listOf(
                        "Visit during sunset for the best views",
                        "Book tickets in advance to avoid queues",
                        "Don't forget your camera"
                    )
                ),
                AudioStop(
                    id = "stop_2",
                    title = "The Observation Deck",
                    description = "Experience breathtaking views from the 124th floor",
                    coordinates = Coordinates(25.197197, 55.274376),
                    audioUrl = "https://example.com/audio/burj_observation.mp3",
                    duration = 240,
                    facts = listOf(
                        "The observation deck is on the 124th floor",
                        "It offers 360-degree views of Dubai",
                        "You can see up to 95 kilometers on a clear day"
                    ),
                    tips = listOf(
                        "Visit early morning for fewer crowds",
                        "Bring binoculars for better views",
                        "Check weather conditions before visiting"
                    )
                )
            ),
            rating = 4.8f,
            downloadCount = 15420,
            isFree = false,
            price = 9.99
        )
        
        // Sheikh Zayed Mosque Tour
        val mosqueTour = AudioTour(
            id = "sheikh_zayed_tour",
            title = "Sheikh Zayed Grand Mosque: A Masterpiece of Islamic Architecture",
            description = "Explore the stunning beauty and spiritual significance of this architectural wonder",
            location = "Sheikh Zayed Grand Mosque, Abu Dhabi",
            duration = 60,
            language = "English",
            audioUrl = "https://example.com/audio/mosque_tour.mp3",
            imageUrl = "https://images.unsplash.com/photo-1542810634-71277d95dcbb?w=800&h=600&fit=crop&crop=center",
            stops = listOf(
                AudioStop(
                    id = "mosque_stop_1",
                    title = "The Grand Entrance",
                    description = "Welcome to one of the world's largest mosques",
                    coordinates = Coordinates(24.4129, 54.4747),
                    audioUrl = "https://example.com/audio/mosque_entrance.mp3",
                    duration = 200,
                    facts = listOf(
                        "The mosque can accommodate 40,000 worshippers",
                        "It features 82 domes of different sizes",
                        "The main prayer hall houses the world's largest chandelier"
                    ),
                    tips = listOf(
                        "Dress modestly when visiting",
                        "Remove shoes before entering",
                        "Photography is allowed but be respectful"
                    )
                )
            ),
            rating = 4.9f,
            downloadCount = 12350,
            isFree = true
        )
        
        audioTours.addAll(listOf(burjKhalifaTour, mosqueTour))
    }
    
    suspend fun playAudioTour(tourId: String): Result<Boolean> {
        return try {
            val tour = audioTours.find { it.id == tourId }
            if (tour != null) {
                currentTour = tour
                currentStop = tour.stops.firstOrNull()
                isPlaying = true
                currentPosition = 0
                
                // Simulate audio playback
                delay(1000)
                
                // Update progress
                val progress = AudioTourProgress(
                    tourId = tourId,
                    currentStop = 0,
                    completedStops = emptyList(),
                    totalDuration = tour.duration * 60,
                    currentPosition = 0,
                    isPlaying = true,
                    lastPlayedAt = System.currentTimeMillis()
                )
                tourProgress[tourId] = progress
                
                Result.success(true)
            } else {
                Result.failure(Exception("Audio tour not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun playStop(stopId: String): Result<Boolean> {
        return try {
            val stop = currentTour?.stops?.find { it.id == stopId }
            if (stop != null) {
                currentStop = stop
                isPlaying = true
                currentPosition = 0
                
                // Simulate audio playback
                delay(stop.duration * 1000L)
                
                // Mark stop as completed
                val tourId = currentTour?.id
                if (tourId != null) {
                    val progress = tourProgress[tourId]
                    if (progress != null) {
                        val completedStops = progress.completedStops.toMutableList()
                        if (!completedStops.contains(stopId)) {
                            completedStops.add(stopId)
                        }
                        
                        tourProgress[tourId] = progress.copy(
                            currentStop = currentTour?.stops?.indexOf(stop) ?: 0,
                            completedStops = completedStops,
                            isPlaying = false
                        )
                    }
                }
                
                Result.success(true)
            } else {
                Result.failure(Exception("Audio stop not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun pauseAudio() {
        isPlaying = false
        currentTour?.id?.let { tourId ->
            tourProgress[tourId]?.let { progress ->
                tourProgress[tourId] = progress.copy(isPlaying = false)
            }
        }
    }
    
    fun resumeAudio() {
        isPlaying = true
        currentTour?.id?.let { tourId ->
            tourProgress[tourId]?.let { progress ->
                tourProgress[tourId] = progress.copy(isPlaying = true)
            }
        }
    }
    
    fun stopAudio() {
        isPlaying = false
        currentPosition = 0
        currentTour = null
        currentStop = null
    }
    
    fun getAudioTours(): List<AudioTour> {
        return audioTours
    }
    
    fun getAudioTour(tourId: String): AudioTour? {
        return audioTours.find { it.id == tourId }
    }
    
    fun getTourProgress(tourId: String): AudioTourProgress? {
        return tourProgress[tourId]
    }
    
    fun getCurrentTour(): AudioTour? {
        return currentTour
    }
    
    fun getCurrentStop(): AudioStop? {
        return currentStop
    }
    
    fun isPlaying(): Boolean {
        return isPlaying
    }
    
    fun getCurrentPosition(): Int {
        return currentPosition
    }
    
    fun seekTo(position: Int) {
        currentPosition = position
        currentTour?.id?.let { tourId ->
            tourProgress[tourId]?.let { progress ->
                tourProgress[tourId] = progress.copy(currentPosition = position)
            }
        }
    }
    
    suspend fun downloadAudioTour(tourId: String): Result<Boolean> {
        return try {
            val tour = audioTours.find { it.id == tourId }
            if (tour != null) {
                // Simulate download
                delay(2000)
                
                // Update download count
                val index = audioTours.indexOf(tour)
                if (index >= 0) {
                    audioTours[index] = tour.copy(downloadCount = tour.downloadCount + 1)
                }
                
                Result.success(true)
            } else {
                Result.failure(Exception("Audio tour not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getToursByLanguage(language: String): List<AudioTour> {
        return audioTours.filter { it.language.equals(language, ignoreCase = true) }
    }
    
    fun getFreeTours(): List<AudioTour> {
        return audioTours.filter { it.isFree }
    }
    
    fun searchTours(query: String): List<AudioTour> {
        return audioTours.filter { tour ->
            tour.title.contains(query, ignoreCase = true) ||
            tour.description.contains(query, ignoreCase = true) ||
            tour.location.contains(query, ignoreCase = true)
        }
    }
} 