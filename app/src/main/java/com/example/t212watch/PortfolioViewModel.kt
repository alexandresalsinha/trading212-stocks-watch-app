package com.example.t212watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
    data class Success(
        val positions: List<Position>,
        val cash: AccountCash?,
    ) : PortfolioUiState
}

class PortfolioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
    val uiState: StateFlow<PortfolioUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = PortfolioUiState.Loading
        viewModelScope.launch {
            try {
                val positions = Trading212Api.service.getPortfolio()
                    .sortedByDescending { it.value }
                val cash = try {
                    Trading212Api.service.getAccountCash()
                } catch (_: Exception) {
                    null
                }
                _uiState.value = PortfolioUiState.Success(positions, cash)
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401 -> "Invalid API key"
                    429 -> "Rate limited, wait a few seconds"
                    else -> "HTTP ${e.code()}"
                }
                _uiState.value = PortfolioUiState.Error(msg)
            } catch (e: IOException) {
                _uiState.value = PortfolioUiState.Error("No connection")
            } catch (e: Exception) {
                _uiState.value = PortfolioUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
