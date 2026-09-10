package com.omar.razorpay.merchant.service.impl;

import com.omar.razorpay.common.exception.ResourceNotFoundException;
import com.omar.razorpay.merchant.entity.Customer;
import com.omar.razorpay.merchant.entity.Merchant;
import com.omar.razorpay.merchant.repository.CustomerRepository;
import com.omar.razorpay.merchant.repository.MerchantRepository;
import com.omar.razorpay.merchant.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;


    @Override
    @Transactional
    public UUID findOrCreate(UUID merchantId, String name, String email, String contactNumber) {
        if(email == null || email.isBlank()){
            return null;
        }
        // Implementation goes here
        return customerRepository.findByMerchant_IdAndEmail(merchantId, email)
                .map(Customer::getId)
                .orElseGet(() -> createNew(merchantId, email, name, contactNumber));
    }

    private UUID createNew(UUID merchantId, String email, String name, String contactNumber) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId.toString()));

        Customer customer = Customer.builder()
                .merchant(merchant)
                .email(email)
                .name(name)
                .contactNumber(contactNumber)
                .build();

        customer = customerRepository.save(customer);

        log.info("Customer {} created", customer.getId());
        return customer.getId();
    }
}
