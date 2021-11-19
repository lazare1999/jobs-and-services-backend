package com.lazo.jc.app.user.services;

import com.lazo.jc.app.main.models.RegisterModel;
import com.lazo.jc.app.user.domains.AppUserDomain;
import com.lazo.jc.app.user.repository.UserRepository;
import com.lazo.jc.security.ApplicationUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static com.lazo.jc.utils.EncryptUtils.encrypt;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Getter
    private JdbcTemplate jdbcTemplate;

    private static final String authoritiesByUsernameQuery = "select user_name, role from users.user_rolesv where user_name=?";
    private static final String usersByUsernameQuery = "select user_id from users.active_users where user_name=?";
    private static final String usersPasswordByUsernameQuery = "select user_passwd from users.active_users where user_name=?";
    private static final String getRoleIdQuery = "select user_role_id from users.user_roles where user_id=? and target_id=?";
    private static final String getRole = "select target_id from users.targets where target_id=?";

    private void updateLastAuthorisedTime(Integer userId) {
        String sql = "UPDATE users.users SET  last_auth_date=now() WHERE user_id=?";
        getJdbcTemplate().update(sql, userId);
    }

    private void logAuthorise(Integer userId, Integer is_success, String remote_address) {
        String sql = "INSERT INTO logs.authorise_history(user_id, is_success, remote_address) VALUES (?, ?, ?);";
        getJdbcTemplate().update(sql, userId, is_success, remote_address);
    }

    public void register(RegisterModel m, String SALT) {
        String sql = "INSERT INTO users.users(phone_number, first_name, last_name, email, nickname, password, personal_number, passport_number, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        getJdbcTemplate().update(sql, m.getFullPhone(), m.getFirstName(), m.getLastName(), m.getEmail(), m.getNickname(), encrypt(SALT, m.getPassword()), m.getPersonalNumber(), m.getPassportNumber(), m.getAddress());
    }

    public void resetPassword(Long userId, String newPassword, String SALT) {
        String sql = "UPDATE users.users SET password=? WHERE user_id=?";
        getJdbcTemplate().update(sql, encrypt(SALT, newPassword), userId);
    }

    private List<GrantedAuthority> loadUserAuthorities(String username) {
        return getJdbcTemplate().query(authoritiesByUsernameQuery, (rs, rowNum) -> {
            String roleName = "" + rs.getString(2);
            return new SimpleGrantedAuthority(roleName);
        }, username);
    }

    private String loadUserPassword(String username) {
        List<String> ans = getJdbcTemplate().query(usersPasswordByUsernameQuery, (rs, rowNum) -> rs.getString(1), username);
        return ans.size()==0? "" : ans.get(0) ;
    }

    private Integer loadUserData(String username) {
        List<Integer> ans = getJdbcTemplate().query(usersByUsernameQuery, (rs, rowNum) -> rs.getInt(1), username);
        return ans.size()==0? 0 : ans.get(0) ;
    }

    protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    protected void initTemplateConfig() {
    }

    public final void setDataSource(DataSource dataSource) {
        if (this.jdbcTemplate == null || dataSource != this.jdbcTemplate.getDataSource()) {
            this.jdbcTemplate = createJdbcTemplate(dataSource);
            initTemplateConfig();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AppUserDomain user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        Integer userId = this.loadUserData(username);
        if(userId == 0){
            return null;
        }

        var aut = loadUserAuthorities(username);

        ApplicationUser ans = null;
        try {
            ans = new ApplicationUser(userId, username, new BCryptPasswordEncoder().encode(user.getPassword()), true, true, true, true, aut);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ans;
    }

    public Boolean checkIfRoleExists(Integer roleId) {
        if (roleId==null)
            return false;

        List<Integer> ans = getJdbcTemplate().query(getRole, (rs, rowNum) -> rs.getInt(1), roleId);

        return !ans.isEmpty();
    }

    public Boolean roleIsAlreadyDefined(Integer userId, Integer roleId) {
        if (userId ==null || roleId==null)
            return false;

        List<Integer> ans = getJdbcTemplate().query(getRoleIdQuery, (rs, rowNum) -> rs.getInt(1), userId, roleId);

        return !ans.isEmpty();
    }

    public Boolean addRole(Integer userId, Integer roleId) {
        if (userId ==null || roleId==null)
            return false;

        String sql = "INSERT INTO users.user_roles(user_id, target_id) VALUES (?, ?);";
        getJdbcTemplate().update(sql, userId, roleId);
        return true;
    }


    private static String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    public Authentication authenticateJwt(String userName, String password, String tempPassword, String code) throws AuthenticationException {
        String remoteAddress = null;
        ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ra != null) {
            remoteAddress = getClientIp(ra.getRequest());
        }

        Integer userId = this.loadUserData(userName);
        if(userId == 0){
            return null;
        }

        var passwordMatches = Objects.equals(password, loadUserPassword(userName));
        var tempPasswordMatches = Objects.equals(tempPassword, code);

        if(!tempPasswordMatches || !passwordMatches) {
            logAuthorise(userId, 0, remoteAddress);
            return null;
        }

        List<GrantedAuthority> grantedAuths = this.loadUserAuthorities(userName);
        ApplicationUser appUser = new ApplicationUser(userId, userName, password, true, true, true, true, grantedAuths);
        updateLastAuthorisedTime(userId);
        logAuthorise(userId, 1, remoteAddress);

        return new UsernamePasswordAuthenticationToken(appUser, password, grantedAuths);
    }

}
