import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";
        
        boolean matches = encoder.matches(password, hash);
        System.out.println("Password matches: " + matches);
        
        // 生成新的哈希值
        String newHash = encoder.encode(password);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(password, newHash));
    }
}


