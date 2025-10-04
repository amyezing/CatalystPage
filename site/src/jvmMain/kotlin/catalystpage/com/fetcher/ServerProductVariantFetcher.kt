package catalystpage.com.fetcher

import catalystpage.com.service.ProductService
import dto.ProductVariantDTO
import fetcher.ProductVariantFetcher

class ServerProductVariantFetcher : ProductVariantFetcher {
    override suspend fun fetchProductVariants(): List<ProductVariantDTO> = ProductService.getProductVariantsWithProductInfo()
}
