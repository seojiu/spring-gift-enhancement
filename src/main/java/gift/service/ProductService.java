package gift.service;

import gift.dto.ProductDto;
import gift.model.Category;
import gift.model.Option;
import gift.model.Product;
import gift.repository.CategoryRepository;
import gift.repository.ProductRepository;
import gift.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WishlistRepository wishlistRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository, WishlistRepository wishlistRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.wishlistRepository = wishlistRepository;
    }

    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product addProduct(ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        List<Option> options = productDto.getOptions().stream()
                .map(optionDto -> new Option(optionDto.getName(), optionDto.getQuantity()))
                .toList();

        if (options.isEmpty()) {
            throw new IllegalArgumentException("상품에는 최소 하나의 옵션이 있어야 합니다.");
        }

        Set<String> optionNames = new HashSet<>();
        for (Option option : options) {
            if (!optionNames.add(option.getName())) {
                throw new IllegalArgumentException("옵션 이름이 중복됩니다: " + option.getName());
            }
        }

        Product product = new Product(
                productDto.getName(),
                productDto.getPrice(),
                productDto.getImageUrl(),
                category,
                options
        );
        productRepository.save(product);
        return product;
    }

    public Product updateProduct(Long id, ProductDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        List<Option> options = productDto.getOptions().stream()
                .map(optionDto -> new Option(optionDto.getName(), optionDto.getQuantity()))
                .toList();

        Set<String> optionNames = new HashSet<>();
        for (Option option : options) {
            if (!optionNames.add(option.getName())) {
                throw new IllegalArgumentException("옵션 이름이 중복됩니다: " + option.getName());
            }
        }

        Product updateProduct = new Product(
                id,
                productDto.getName(),
                productDto.getPrice(),
                productDto.getImageUrl(),
                category,
                options
        );
        return productRepository.save(updateProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        wishlistRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
    }

    public void updateProductCategoryToNone(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        Category noneCategory = categoryRepository.findById(1L)
                .orElseThrow(() -> new NoSuchElementException("없음 카테고리를 찾을 수 없습니다."));

        List<Product> products = productRepository.findByCategory(category);
        for (Product product : products) {
            Product updateProduct = new Product(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    product.getImageUrl(),
                    noneCategory,
                    product.getOptions()
            );
            productRepository.save(updateProduct);
        }
    }
}
