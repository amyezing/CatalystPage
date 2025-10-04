package catalystpage.com.wrapper

import dto.ProductDTO

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<ProductDTO>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}