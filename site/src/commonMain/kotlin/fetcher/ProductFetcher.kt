package fetcher

import dto.ProductDTO

interface ProductFetcher {
    suspend fun fetchProducts(): List<ProductDTO>
}