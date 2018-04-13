/*package pl.radekbonk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.radekbonk.entity.ProductEntity;
import pl.radekbonk.repository.ProductRepository;

@Service
public class ProductsService {

	@Autowired
	private ProductRepository productRepository;

	public Iterable<ProductEntity> getAllProducts() {
		return this.productRepository.findAll();
	}

	public ProductEntity getProductById(long id) {
		return this.productRepository.findOne(id);
	}
}*/
