package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.enums.TransactionType;
import com.wonwire.wonwire.dto.ContactResponseDTO;
import com.wonwire.wonwire.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ContactService contactService;

    private User alice;
    private User bob;
    private User charlie;

    @BeforeEach
    void setUp() {
        alice = User.builder()
                .email("alice@wonwire.com")
                .firstName("Alice")
                .lastName("Smith")
                .password("encoded")
                .build();

        bob = User.builder()
                .email("bob@wonwire.com")
                .firstName("Bob")
                .lastName("Martin")
                .password("encoded")
                .build();

        charlie = User.builder()
                .email("charlie@wonwire.com")
                .firstName("Charlie")
                .lastName("Brown")
                .password("encoded")
                .build();
    }

    // -------------------------------------------------------------------------
    // getContacts()
    // -------------------------------------------------------------------------

    @Test
    void getContacts_ShouldReturnUniqueContacts_WhenUserHasBothSentAndReceived() {
        // Given — Alice sent to Bob, received from Charlie
        when(transactionRepository.findReceiversBySender(alice, TransactionType.TRANSFER))
                .thenReturn(List.of(bob));
        when(transactionRepository.findSendersByReceiver(alice, TransactionType.TRANSFER))
                .thenReturn(List.of(charlie));

        // When
        List<ContactResponseDTO> contacts = contactService.getContacts(alice);

        // Then
        assertThat(contacts).hasSize(2);
        assertThat(contacts.get(0).getEmail()).isEqualTo(bob.getEmail());
        assertThat(contacts.get(1).getEmail()).isEqualTo(charlie.getEmail());
    }

    @Test
    void getContacts_ShouldDeduplicateContacts_WhenUserExchangedWithSamePerson() {
        // Given — Alice sent to Bob AND received from Bob
        when(transactionRepository.findReceiversBySender(alice, TransactionType.TRANSFER))
                .thenReturn(List.of(bob));
        when(transactionRepository.findSendersByReceiver(alice, TransactionType.TRANSFER))
                .thenReturn(List.of(bob));

        // When
        List<ContactResponseDTO> contacts = contactService.getContacts(alice);

        // Then — Bob appears only once despite being in both lists
        assertThat(contacts).hasSize(1);
        assertThat(contacts.get(0).getEmail()).isEqualTo(bob.getEmail());
    }

    @Test
    void getContacts_ShouldReturnEmptyList_WhenUserHasNoTransactions() {
        // Given
        when(transactionRepository.findReceiversBySender(alice, TransactionType.TRANSFER))
                .thenReturn(List.of());
        when(transactionRepository.findSendersByReceiver(alice, TransactionType.TRANSFER))
                .thenReturn(List.of());

        // When
        List<ContactResponseDTO> contacts = contactService.getContacts(alice);

        // Then
        assertThat(contacts).isEmpty();
    }

    @Test
    void getContacts_ShouldMapContactFieldsCorrectly() {
        // Given
        when(transactionRepository.findReceiversBySender(alice, TransactionType.TRANSFER))
                .thenReturn(List.of(bob));
        when(transactionRepository.findSendersByReceiver(alice, TransactionType.TRANSFER))
                .thenReturn(List.of());

        // When
        List<ContactResponseDTO> contacts = contactService.getContacts(alice);

        // Then
        assertThat(contacts.get(0).getFirstName()).isEqualTo("Bob");
        assertThat(contacts.get(0).getLastName()).isEqualTo("Martin");
        assertThat(contacts.get(0).getEmail()).isEqualTo("bob@wonwire.com");
    }
}