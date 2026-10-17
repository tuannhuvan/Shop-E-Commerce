package org.example.demofullstackspringboot.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demofullstackspringboot.entity.*;
import org.example.demofullstackspringboot.repository.CategoryRepository;
import org.example.demofullstackspringboot.repository.CustomerRepository;
import org.example.demofullstackspringboot.repository.OrderRepository;
import org.example.demofullstackspringboot.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Dữ liệu ban đầu đã tồn tại, bỏ qua Data Seeder.");
            return;
        }

        log.info("Đang khởi tạo dữ liệu mẫu (Database Seeding)...");

        // 1. Categories
        Category cat1 = Category.builder()
                .id("cat001")
                .name("Laptop & Máy Tính")
                .slug("laptop-may-tinh")
                .description("Laptop văn phòng, Gaming và đồ họa cao cấp")
                .createdAt(LocalDateTime.now())
                .build();

        Category cat2 = Category.builder()
                .id("cat002")
                .name("Điện Thoại & Tablet")
                .slug("dien-thoai-tablet")
                .description("Smartphone, iPhone, iPad và Máy tính bảng")
                .createdAt(LocalDateTime.now())
                .build();

        Category cat3 = Category.builder()
                .id("cat003")
                .name("Phụ Kiện Công Nghệ")
                .slug("phu-kien-cong-nghe")
                .description("Bàn phím cơ, chuột không dây, cáp sạc nhanh")
                .createdAt(LocalDateTime.now())
                .build();

        Category cat4 = Category.builder()
                .id("cat004")
                .name("Âm Thanh & Tai Nghe")
                .slug("am-thanh-tai-nghe")
                .description("Tai nghe chống ồn, loa Bluetooth di động")
                .createdAt(LocalDateTime.now())
                .build();

        categoryRepository.save(cat1);
        categoryRepository.save(cat2);
        categoryRepository.save(cat3);
        categoryRepository.save(cat4);

        // 2. Customers
        Customer cust1 = Customer.builder()
                .id("c101")
                .fullName("Nguyễn Văn An")
                .email("an.nguyen@test.com")
                .phone("0912345678")
                .address("123 Lê Lợi, Quận 1, TP.HCM")
                .createdAt(LocalDateTime.now())
                .build();

        Customer cust2 = Customer.builder()
                .id("c102")
                .fullName("Trần Thị Bình")
                .email("binh.tran@test.com")
                .phone("0987654321")
                .address("456 Hoàn Kiếm, Hà Nội")
                .createdAt(LocalDateTime.now())
                .build();

        Customer cust3 = Customer.builder()
                .id("c103")
                .fullName("Lê Hoàng Nam")
                .email("nam.le@test.com")
                .phone("0901234567")
                .address("789 Nguyễn Huệ, Quận Hải Châu, Đà Nẵng")
                .createdAt(LocalDateTime.now())
                .build();

        customerRepository.save(cust1);
        customerRepository.save(cust2);
        customerRepository.save(cust3);

        // 3. Products
        Product p1 = Product.builder()
                .id("p201")
                .category(cat1)
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("35000000.00"))
                .quantity(25)
                .description("Intel Core i9 thế hệ mới, RAM 32GB, SSD 1TB, màn hình 3.5K OLED sắc nét")
                .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        Product p2 = Product.builder()
                .id("p202")
                .category(cat2)
                .name("iPhone 16 Pro Max")
                .price(new BigDecimal("34990000.00"))
                .quantity(40)
                .description("Chip A18 Pro 256GB Titan Sa Mạc, Camera 48MP zoom quang 5x đỉnh cao")
                .imageUrl("https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        Product p3 = Product.builder()
                .id("p203")
                .category(cat1)
                .name("MacBook Pro 16 M3 Max")
                .price(new BigDecimal("68990000.00"))
                .quantity(15)
                .description("Apple M3 Max 36GB RAM, 1TB SSD Liquid Retina XDR ProMotion 120Hz")
                .imageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        Product p4 = Product.builder()
                .id("p204")
                .category(cat2)
                .name("Samsung Galaxy S24 Ultra")
                .price(new BigDecimal("29990000.00"))
                .quantity(30)
                .description("Snapdragon 8 Gen 3 for Galaxy, Bút S-Pen tích hợp, Trợ lý AI Galaxy thông minh")
                .imageUrl("https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        Product p5 = Product.builder()
                .id("p205")
                .category(cat4)
                .name("Tai nghe Sony WH-1000XM5")
                .price(new BigDecimal("7490000.00"))
                .quantity(50)
                .description("Chống ồn chủ động hàng đầu ngành, thời lượng pin 30 giờ, âm thanh Hi-Res LDAC")
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        Product p6 = Product.builder()
                .id("p206")
                .category(cat3)
                .name("Bàn phím cơ Keychron Q1 Pro")
                .price(new BigDecimal("4500000.00"))
                .quantity(20)
                .description("Vỏ nhôm CNC nguyên khối, kết nối Bluetooth không dây, Switch Gateron Jupiter gõ cực êm")
                .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500")
                .createdAt(LocalDateTime.now())
                .build();

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);
        productRepository.save(p6);

        // 4. Initial Orders
        Order order1 = Order.builder()
                .id("o301")
                .customer(cust1)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("35000000.00"))
                .createdAt(LocalDateTime.now().minusHours(3))
                .items(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id("i401")
                .order(order1)
                .product(p1)
                .quantity(1)
                .unitPrice(new BigDecimal("35000000.00"))
                .subtotal(new BigDecimal("35000000.00"))
                .build();
        order1.addItem(item1);

        Order order2 = Order.builder()
                .id("o302")
                .customer(cust2)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("34990000.00"))
                .createdAt(LocalDateTime.now().minusHours(1))
                .items(new ArrayList<>())
                .build();

        OrderItem item2 = OrderItem.builder()
                .id("i402")
                .order(order2)
                .product(p2)
                .quantity(1)
                .unitPrice(new BigDecimal("34990000.00"))
                .subtotal(new BigDecimal("34990000.00"))
                .build();
        order2.addItem(item2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        log.info("Database Seeding hoàn tất thành công! Sẵn sàng phục vụ yêu cầu.");
    }
}
