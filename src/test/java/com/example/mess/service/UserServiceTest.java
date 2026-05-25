package com.example.mess.service;

import com.example.mess.entity.User;
import com.example.mess.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
    }

    @Test
    void getAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));
        
        List<User> users = userService.getAllUsers();
        
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Optional<User> user = userService.getUserById(1L);
        
        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void createUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User createdUser = userService.createUser(testUser);
        
        assertNotNull(createdUser);
        assertEquals("testuser", createdUser.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        
        userService.deleteUser(1L);
        
        verify(userRepository, times(1)).deleteById(1L);
    }
}