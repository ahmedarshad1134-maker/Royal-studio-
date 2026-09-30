package com.example

import com.example.data.models.PackageDto
import com.example.data.models.StudioSettingsDto
import com.example.ui.viewmodels.AboutViewModel
import com.example.ui.viewmodels.ContactViewModel
import com.example.ui.viewmodels.PackageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `PackageViewModel falls back to hardcoded packages when Firebase is uninitialized`() = runTest {
        val viewModel = PackageViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.packages.isNotEmpty())
        assertEquals(4, state.packages.size)
        assertEquals("pkg_basic", state.packages[0].id)
        assertNotNull(viewModel.getPackageById("pkg_standard"))
    }

    @Test
    fun `AboutViewModel falls back to hardcoded about info when Firebase is uninitialized`() = runTest {
        val viewModel = AboutViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.aboutInfo)
        assertEquals("Royal Studio", state.aboutInfo?.studioName)
        assertTrue(state.aboutInfo?.teamMembers?.isNotEmpty() == true)
    }

    @Test
    fun `ContactViewModel falls back to hardcoded contact info when Firebase is uninitialized`() = runTest {
        val viewModel = ContactViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.contactInfo)
        assertEquals("+916289172657", state.contactInfo?.phone)
        assertEquals("+916289172657", state.contactInfo?.whatsapp)
        assertEquals("F-55/A Battikal 2nd lean", state.contactInfo?.address)
    }
}
