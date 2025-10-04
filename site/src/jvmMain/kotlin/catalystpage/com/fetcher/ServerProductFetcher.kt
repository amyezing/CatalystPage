package catalystpage.com.fetcher

import catalystpage.com.service.ProductService.getAll
import dto.ProductDTO
import fetcher.ProductFetcher

class ServerProductFetcher : ProductFetcher {
    override suspend fun fetchProducts(): List<ProductDTO> {
        return getAll() // Assuming this is a function that fetches products from the DB
    }
}