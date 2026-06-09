package com.example.project.service;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Xác thực đăng nhập của người dùng.
     * @param username tên đăng nhập
     * @param password mật khẩu chưa mã hóa (plain text)
     * @return User nếu thông tin chính xác, ngược lại là Optional.empty()
     */
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Đăng ký người dùng mới (Mã hóa mật khẩu bằng BCrypt).
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Lưu thông tin user. Nếu password chưa được mã hóa BCrypt (không bắt đầu bằng $2a$),
     * tự động encode trước khi lưu để tránh lưu plain text vào DB.
     */
    public User saveUser(User user) {
        String pwd = user.getPassword();
        if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
            user.setPassword(passwordEncoder.encode(pwd));
        }
        return userRepository.save(user);
    }

    public String encodePassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
