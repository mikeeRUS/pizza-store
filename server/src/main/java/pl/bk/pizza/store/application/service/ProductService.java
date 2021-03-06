package pl.bk.pizza.store.application.service;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.bk.pizza.store.application.dto.product.input.NewProductDTO;
import pl.bk.pizza.store.application.dto.product.input.NewProductPriceDTO;
import pl.bk.pizza.store.application.dto.product.output.ProductDTO;
import pl.bk.pizza.store.application.mapper.product.NewProductMapper;
import pl.bk.pizza.store.application.mapper.product.ProductMapper;
import pl.bk.pizza.store.domain.product.BaseProductInfo;
import pl.bk.pizza.store.domain.product.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static pl.bk.pizza.store.domain.validator.product.ProductValidator.productShouldExists;

@Service
@AllArgsConstructor
public class ProductService
{
    private final ProductRepository productRepository;
    private final NewProductMapper newProductMapper;
    private final ProductMapper productMapper;
    private final ReactiveMongoTemplate mongoTemplate;
    
    public Mono<ProductDTO> createProduct(NewProductDTO newProduct)
    {
        return Mono.just(newProductMapper.mapFromDTO(newProduct))
                   .flatMap(productRepository::save)
                   .map(productMapper::mapToDTO);
    }
    
    public Mono<ProductDTO> getProduct(String productId)
    {
        return productRepository
            .findById(productId)
            .doOnNext(it -> productShouldExists(it, productId))
            .map(productMapper::mapToDTO);
    }
    
    public Flux<ProductDTO> getAllProducts()
    {
        return productRepository
            .findAll()
            .map(productMapper::mapToDTO);
    }
    
    public Flux<ProductDTO> getAllAvailableProducts()
    {
        return productRepository
            .findAll()
            .filter(BaseProductInfo::isAvailable)
            .map(productMapper::mapToDTO);
    }
    
    public Mono<ProductDTO> changeProductPrice(String productId, NewProductPriceDTO newProductPriceDTO)
    {
        return productRepository
            .findById(productId)
            .doOnNext(it -> productShouldExists(it, productId))
            .doOnNext(product -> product.changePrice(newProductPriceDTO.getPrice()))
            .flatMap(productRepository::save)
            .map(productMapper::mapToDTO);
    }
    
    public Mono<ProductDTO> makeProductNonAvailable(String productId)
    {
        return productRepository
            .findById(productId)
            .doOnNext(it -> productShouldExists(it, productId))
            .doOnNext(BaseProductInfo::makeNonavailable)
            .flatMap(productRepository::save)
            .map(productMapper::mapToDTO);
    }
    
    public Flux<ProductDTO> getAllProducts(Class<? extends BaseProductInfo> clazz)
    {
        final Query query = new Query().restrict(clazz);
        return mongoTemplate.find(query, clazz, "product")
            .map(productMapper::mapToDTO);
    }
}
