
package vn.rikkei.cinemabox.customer;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.rikkei.cinemabox.common.*;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest req) {

        if (repository.existsByEmail(req.email())) {
            throw new ConflictException("Email đã tồn tại");
        }

        Customer customer = new Customer();

        customer.setFullName(req.fullName().trim());
        customer.setEmail(req.email());
        customer.setPhone(req.phone());

        customer.setMemberLevel(
                req.memberLevel() == null
                        ? "STANDARD"
                        : req.memberLevel()
        );

        return CustomerResponse.from(
                repository.saveAndFlush(customer)
        );
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy khách hàng id=" + id
                        ));

        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new BadRequestException(
                    "Page hoặc size không hợp lệ"
            );
        }

        Page<CustomerResponse> result = repository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").ascending()
                )
        ).map(CustomerResponse::from);

        return PageResponse.from(result);
    }
}
