package fetcher

import dto.ProductVariantDTO

interface ProductVariantFetcher {
    suspend fun fetchProductVariants(): List<ProductVariantDTO>
}