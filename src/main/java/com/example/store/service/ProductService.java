package com.example.store.service;

import com.example.store.dto.CreateProductDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.exception.NotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDTO createProduct(@Valid CreateProductDTO dto) {
        return productMapper.productToProductDTO(productRepository.save(productMapper.createDTOToProduct(dto)));
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productMapper.productsToProductDTOs(productRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productRepository
                .findWithOrderById(id)
                .map(productMapper::productToProductDTO)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }
}
