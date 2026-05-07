package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.enums.TransactionType;
import com.wonwire.wonwire.dto.ContactResponseDTO;
import com.wonwire.wonwire.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final TransactionRepository transactionRepository;

    /**
     * Returns the list of unique contacts for the authenticated user.
     * A contact is any user the authenticated user has exchanged money with, whether as sender or receiver.
     * Deposits are excluded.
     */
    public List<ContactResponseDTO> getContacts(User user) {
        List<User> sentTo = transactionRepository.findReceiversBySender(user, TransactionType.TRANSFER);
        List<User> receivedFrom = transactionRepository.findSendersByReceiver(user, TransactionType.TRANSFER);

        // Use a Set on email to deduplicate users that appear in both lists
        Set<String> seenEmails = new LinkedHashSet<>();
        List<ContactResponseDTO> contacts = new ArrayList<>();

        for (User contact : sentTo) {
            if (seenEmails.add(contact.getEmail())) {
                contacts.add(toDTO(contact));
            }
        }

        for (User contact : receivedFrom) {
            if (seenEmails.add(contact.getEmail())) {
                contacts.add(toDTO(contact));
            }
        }

        return contacts;
    }

    private ContactResponseDTO toDTO(User user) {
        return ContactResponseDTO.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}