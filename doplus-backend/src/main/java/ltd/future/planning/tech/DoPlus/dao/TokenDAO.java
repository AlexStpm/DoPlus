package ltd.future.planning.tech.DoPlus.dao;


import lombok.RequiredArgsConstructor;
import ltd.future.planning.tech.DoPlus.entity.Token;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenDAO {

    private final JdbcTemplate jdbcTemplate;

    public List<Token> findByUser(int id) {
        return this.jdbcTemplate.query("select t.token_id, t.token, t.user_id  from Tokens t inner join Users u\n" +
                        "      on t.user_id = u.user_id where u.user_id =  " + id + "; ",
                (resultSet, rowNum) -> new Token(
                        resultSet.getInt("token_id"),
                        resultSet.getString("token"),
                        resultSet.getInt("user_id"))
        );
    }

    public Optional<Token> getByToken(String token) {
        return this.jdbcTemplate.query("SELECT * " +
                        "FROM tokens " +
                        "WHERE token = '" + token + "'",
                (resultSet) -> {
                    if (resultSet.next()) {
                        return Optional.of(new Token(
                                resultSet.getInt("token_id"),
                                resultSet.getString("token"),
                                resultSet.getInt("user_id")));
                    }
                    return Optional.empty();
                });
    }

    public void add(Token token) {
        String sql = "INSERT INTO tokens (token, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, token.getToken(), token.getUserId());
    }

    public void delete(String token) {
        String sql = "DELETE FROM tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
}
