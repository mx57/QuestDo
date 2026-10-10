package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.SoundEffectsHelper
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SoundEffectsHelperTest {

    @Test
    fun testAmbientSoundBufferGenerators() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = SoundEffectsHelper(context)
        val bufferSize = 2048
        val buffer = ShortArray(bufferSize)
        val random = Random(42)

        // Test Rain Generator
        val nextRainFilter = helper.generateRainBuffer(buffer, random, 0.0)
        assertNotEquals(0.0, nextRainFilter)
        assertTrue(buffer.any { it != 0.toShort() })

        // Test White Noise Generator
        val nextWhiteFilter = helper.generateWhiteNoiseBuffer(buffer, random, 0.0)
        assertNotEquals(0.0, nextWhiteFilter)
        assertTrue(buffer.any { it != 0.toShort() })

        // Test Campfire Generator
        val nextCampfireFilter = helper.generateCampfireBuffer(buffer, random, 0.0)
        assertNotEquals(0.0, nextCampfireFilter)
        assertTrue(buffer.any { it != 0.toShort() })

        // Test Cosmos Generator
        val nextCosmosPhase = helper.generateCosmosBuffer(buffer, random, 0.0, 22050)
        assertNotEquals(0.0, nextCosmosPhase)
        assertTrue(buffer.any { it != 0.toShort() })

        // Test Default Whisper Generator
        val nextWhisperFilter = helper.generateDefaultWhisperBuffer(buffer, random, 0.0)
        assertNotEquals(0.0, nextWhisperFilter)
        assertTrue(buffer.any { it != 0.toShort() })
    }
}
