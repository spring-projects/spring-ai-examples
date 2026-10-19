/*
* Copyright 2026 - 2026 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.ai.tool.annotation.Tool;

/**
 * @author Christian Tzolov
 */

public class DummyTools {

	// ============== Records ==============

	public record Product(Long id, String name, String description) {
	}

	public record Perfume(Long id, String name, String brand) {
	}

	public record PerfumeVariation(Long id, String scent, int volume) {
	}

	public record Shoes(Long id, String brand, String model, String size) {
	}

	public record ShoesVariation(Long id, String color, int size) {
	}

	// ============== In-Memory Repositories ==============

	private final Map<Long, Product> productRepository = new ConcurrentHashMap<>();

	private final Map<Long, Perfume> perfumeRepository = new ConcurrentHashMap<>();

	private final Map<Long, PerfumeVariation> perfumeVariationRepository = new ConcurrentHashMap<>();

	private final Map<Long, Shoes> shoesRepository = new ConcurrentHashMap<>();

	private final Map<Long, ShoesVariation> shoesVariationRepository = new ConcurrentHashMap<>();

	// ID generators for each entity type
	private final AtomicLong productIdGenerator = new AtomicLong(100);

	private final AtomicLong perfumeIdGenerator = new AtomicLong(100);

	private final AtomicLong perfumeVariationIdGenerator = new AtomicLong(100);

	private final AtomicLong shoesIdGenerator = new AtomicLong(100);

	private final AtomicLong shoesVariationIdGenerator = new AtomicLong(100);

	// ============== Constructor with Sample Data ==============

	public DummyTools() {
		initializeSampleData();
	}

	private void initializeSampleData() {
		// Sample Products
		productRepository.put(1L,
				new Product(1L, "Laptop Pro 15", "High-performance laptop with 16GB RAM and 512GB SSD"));
		productRepository.put(2L, new Product(2L, "Wireless Mouse", "Ergonomic wireless mouse with long battery life"));
		productRepository.put(3L, new Product(3L, "USB-C Hub", "7-in-1 USB-C hub with HDMI and card reader"));
		productRepository.put(4L,
				new Product(4L, "Mechanical Keyboard", "RGB mechanical keyboard with Cherry MX switches"));
		productRepository.put(5L, new Product(5L, "Monitor 27\"", "4K UHD monitor with HDR support"));

		// Sample Perfumes
		perfumeRepository.put(1L, new Perfume(1L, "Eau de Cologne", "Chanel"));
		perfumeRepository.put(2L, new Perfume(2L, "Floral Mist", "Dior"));
		perfumeRepository.put(3L, new Perfume(3L, "Ocean Breeze", "Versace"));
		perfumeRepository.put(4L, new Perfume(4L, "Night Essence", "Tom Ford"));
		perfumeRepository.put(5L, new Perfume(5L, "Fresh Citrus", "Gucci"));

		// Sample Perfume Variations
		perfumeVariationRepository.put(1L, new PerfumeVariation(1L, "Citrus", 100));
		perfumeVariationRepository.put(2L, new PerfumeVariation(2L, "Woody", 50));
		perfumeVariationRepository.put(3L, new PerfumeVariation(3L, "Floral", 75));
		perfumeVariationRepository.put(4L, new PerfumeVariation(4L, "Musky", 100));
		perfumeVariationRepository.put(5L, new PerfumeVariation(5L, "Fresh", 30));

		// Sample Shoes
		shoesRepository.put(1L, new Shoes(1L, "Nike", "Air Max", "10"));
		shoesRepository.put(2L, new Shoes(2L, "Adidas", "Ultraboost", "9"));
		shoesRepository.put(3L, new Shoes(3L, "Puma", "RS-X", "11"));
		shoesRepository.put(4L, new Shoes(4L, "New Balance", "574", "10"));
		shoesRepository.put(5L, new Shoes(5L, "Reebok", "Classic Leather", "8"));

		// Sample Shoes Variations
		shoesVariationRepository.put(1L, new ShoesVariation(1L, "Black", 10));
		shoesVariationRepository.put(2L, new ShoesVariation(2L, "White", 9));
		shoesVariationRepository.put(3L, new ShoesVariation(3L, "Red", 11));
		shoesVariationRepository.put(4L, new ShoesVariation(4L, "Blue", 10));
		shoesVariationRepository.put(5L, new ShoesVariation(5L, "Gray", 8));
	}

	// ============== Product Operations ==============

	@Tool(description = "Save a new product to the inventory catalog. Creates a new product entry with the provided name and description. If no ID is provided, a unique identifier will be automatically generated. Returns the saved product with its assigned ID. Use this when adding new merchandise, electronics, or general items to the product database.")
	public Product saveProduct(Product product) {
		Long id = product.id() != null ? product.id() : productIdGenerator.getAndIncrement();
		Product newProduct = new Product(id, product.name(), product.description());
		productRepository.put(id, newProduct);
		return newProduct;
	}

	@Tool(description = "Retrieve a specific product from the inventory by its unique identifier. Returns the complete product details including name and description if found, or empty if the product doesn't exist. Use this to look up individual product information, verify product existence, or fetch details for display purposes.")
	public Optional<Product> getProductById(Long id) {
		return Optional.ofNullable(productRepository.get(id));
	}

	@Tool(description = "Retrieve the complete list of all products in the inventory catalog. Returns all available products with their IDs, names, and descriptions. Use this for inventory browsing, generating product catalogs, or displaying all available merchandise to users.")
	public List<Product> getAllProducts() {
		return new ArrayList<>(productRepository.values());
	}

	@Tool(description = "Permanently remove a product from the inventory catalog by its unique identifier. This action is irreversible and will delete all associated product data. Use this when discontinuing a product, removing outdated items, or cleaning up test data from the catalog.")
	public void deleteProduct(Long id) {
		productRepository.remove(id);
	}

	@Tool(description = "Save a new perfume or fragrance to the beauty and cosmetics collection. Creates a new perfume entry with the specified name and brand information. If no ID is provided, a unique identifier will be automatically generated. Returns the saved perfume with its assigned ID. Use this when adding new fragrances, colognes, or scented products.")
	public Perfume savePerfume(Perfume perfume) {
		Long id = perfume.id() != null ? perfume.id() : perfumeIdGenerator.getAndIncrement();
		Perfume newPerfume = new Perfume(id, perfume.name(), perfume.brand());
		perfumeRepository.put(id, newPerfume);
		return newPerfume;
	}

	@Tool(description = "Retrieve a specific perfume or fragrance from the beauty collection by its unique identifier. Returns complete perfume details including name and brand if found, or empty if the perfume doesn't exist. Use this to look up individual fragrance information or verify perfume availability.")
	public Optional<Perfume> getPerfumeById(Long id) {
		return Optional.ofNullable(perfumeRepository.get(id));
	}

	@Tool(description = "Search for a perfume or fragrance by its exact name in the beauty collection. Performs case-insensitive matching to find the fragrance. Returns the perfume details including brand information if found. Use this when customers know the perfume name but not the ID.")
	public Optional<Perfume> getPerfumeByName(String name) {
		return perfumeRepository.values().stream().filter(p -> p.name().equalsIgnoreCase(name)).findFirst();
	}

	@Tool(description = "Retrieve the complete list of all perfumes and fragrances in the beauty collection. Returns all available perfumes with their IDs, names, and brand information. Use this for displaying fragrance catalogs, browsing available scents, or generating beauty product reports.")
	public List<Perfume> getAllPerfumes() {
		return new ArrayList<>(perfumeRepository.values());
	}

	@Tool(description = "Permanently remove a perfume or fragrance from the beauty collection by its unique identifier. This action is irreversible and will delete all associated perfume data. Use this when discontinuing a fragrance, removing outdated scents, or cleaning up test entries from the collection.")
	public void deletePerfume(Long id) {
		perfumeRepository.remove(id);
	}

	// ============== Perfume Variation Operations ==============

	@Tool(description = "Save a new perfume variation to the fragrance options catalog. Creates a variation entry specifying different scent profiles and bottle volumes in milliliters. If no ID is provided, a unique identifier will be automatically generated. Returns the saved variation with its assigned ID. Use this when adding size options or scent variations for existing fragrances.")
	public PerfumeVariation savePerfumeVariation(PerfumeVariation variation) {
		Long id = variation.id() != null ? variation.id() : perfumeVariationIdGenerator.getAndIncrement();
		PerfumeVariation newVariation = new PerfumeVariation(id, variation.scent(), variation.volume());
		perfumeVariationRepository.put(id, newVariation);
		return newVariation;
	}

	@Tool(description = "Retrieve a specific perfume variation from the fragrance options by its unique identifier. Returns complete variation details including scent type and volume if found, or empty if the variation doesn't exist. Use this to look up specific fragrance options or verify variation availability.")
	public Optional<PerfumeVariation> getPerfumeVariationById(Long id) {
		return Optional.ofNullable(perfumeVariationRepository.get(id));
	}

	@Tool(description = "Retrieve the complete list of all perfume variations in the fragrance options catalog. Returns all available variations with their IDs, scent profiles, and bottle volumes. Use this for displaying available fragrance sizes and scent options to customers.")
	public List<PerfumeVariation> getAllPerfumeVariations() {
		return new ArrayList<>(perfumeVariationRepository.values());
	}

	@Tool(description = "Search for perfume variations by bottle volume in milliliters. Finds variations matching the specified capacity size. Returns the first matching variation if found. Use this when customers are looking for a specific bottle size like 30ml, 50ml, or 100ml options.")
	public Optional<PerfumeVariation> getPerfumeVariationsByVolume(int volume) {
		return perfumeVariationRepository.values().stream().filter(v -> v.volume() == volume).findFirst();
	}

	@Tool(description = "Permanently remove a perfume variation from the fragrance options catalog by its unique identifier. This action is irreversible and will delete the variation data. Use this when discontinuing a specific size or scent option, or cleaning up test variation entries.")
	public void deletePerfumeVariation(Long id) {
		perfumeVariationRepository.remove(id);
	}

	// ============== Shoes Operations ==============

	@Tool(description = "Save a new pair of shoes to the footwear inventory. Creates a shoes entry with brand, model, and size information. If no ID is provided, a unique identifier will be automatically generated. Returns the saved shoes item with its assigned ID. Use this when adding new athletic footwear, casual shoes, or formal footwear to the catalog.")
	public Shoes saveShoes(Shoes shoes) {
		Long id = shoes.id() != null ? shoes.id() : shoesIdGenerator.getAndIncrement();
		Shoes newShoes = new Shoes(id, shoes.brand(), shoes.model(), shoes.size());
		shoesRepository.put(id, newShoes);
		return newShoes;
	}

	@Tool(description = "Retrieve a specific pair of shoes from the footwear inventory by its unique identifier. Returns complete shoes details including brand, model, and size if found, or empty if the shoes don't exist. Use this to look up individual footwear information or verify shoes availability in stock.")
	public Optional<Shoes> getShoesById(Long id) {
		return Optional.ofNullable(shoesRepository.get(id));
	}

	@Tool(description = "Retrieve the complete list of all shoes in the footwear inventory. Returns all available shoes with their IDs, brands, models, and sizes. Use this for displaying footwear catalogs, browsing available shoe styles, or generating inventory reports for the shoe department.")
	public List<Shoes> getAllShoes() {
		return new ArrayList<>(shoesRepository.values());
	}

	@Tool(description = "Permanently remove a pair of shoes from the footwear inventory by its unique identifier. This action is irreversible and will delete all associated shoes data. Use this when discontinuing a shoe model, removing sold-out items, or cleaning up test footwear entries from the catalog.")
	public void deleteShoes(Long id) {
		shoesRepository.remove(id);
	}

	// ============== Shoes Variation Operations ==============

	@Tool(description = "Save a new shoes variation to the footwear options catalog. Creates a variation entry specifying different color options and numeric sizes. If no ID is provided, a unique identifier will be automatically generated. Returns the saved variation with its assigned ID. Use this when adding color variants or size options for existing shoe models.")
	public ShoesVariation saveShoesVariation(ShoesVariation variation) {
		Long id = variation.id() != null ? variation.id() : shoesVariationIdGenerator.getAndIncrement();
		ShoesVariation newVariation = new ShoesVariation(id, variation.color(), variation.size());
		shoesVariationRepository.put(id, newVariation);
		return newVariation;
	}

	@Tool(description = "Retrieve a specific shoes variation from the footwear options by its unique identifier. Returns complete variation details including color and numeric size if found, or empty if the variation doesn't exist. Use this to look up specific shoe color and size combinations.")
	public Optional<ShoesVariation> getShoesVariationById(Long id) {
		return Optional.ofNullable(shoesVariationRepository.get(id));
	}

	@Tool(description = "Retrieve the complete list of all shoes variations in the footwear options catalog. Returns all available variations with their IDs, colors, and numeric sizes. Use this for displaying available shoe colors and sizes to customers during shopping.")
	public List<ShoesVariation> getAllShoesVariations() {
		return new ArrayList<>(shoesVariationRepository.values());
	}

	@Tool(description = "Search for shoes variations by numeric size. Finds variations matching the specified shoe size number. Returns the first matching variation if found. Use this when customers are looking for shoes in a specific size like size 8, 9, 10, or 11.")
	public Optional<ShoesVariation> getShoesVariationsBySize(int size) {
		return shoesVariationRepository.values().stream().filter(v -> v.size() == size).findFirst();
	}

	@Tool(description = "Permanently remove a shoes variation from the footwear options catalog by its unique identifier. This action is irreversible and will delete the variation data. Use this when discontinuing a specific color or size option, or cleaning up test variation entries.")
	public void deleteShoesVariation(Long id) {
		shoesVariationRepository.remove(id);
	}

}
