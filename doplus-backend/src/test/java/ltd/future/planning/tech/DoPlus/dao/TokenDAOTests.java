package ltd.future.planning.tech.DoPlus.dao;

import ltd.future.planning.tech.DoPlus.entity.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenDAOTests {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TokenDAO tokenDAO;

    @Mock
    private ResultSet resultSet;

    //2- /authenticate
    @Test
    void userIdGivenFindByUserCorrectlyMapsToken() throws SQLException {
        int userId = 1;
        when(resultSet.getInt("token_id")).thenReturn(1);
        when(resultSet.getString("token")).thenReturn("token123");
        when(resultSet.getInt("user_id")).thenReturn(userId);

        when(jdbcTemplate.query(eq("select t.token_id, t.token, t.user_id  from Tokens t inner join Users u\n" +
                "      on t.user_id = u.user_id where u.user_id =  " + userId + "; "), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<Token> rowMapper = invocation.getArgument(1);
                    return List.of(rowMapper.mapRow(resultSet, 1));
                });

        List<Token> result = tokenDAO.findByUser(userId);

        assertEquals(1, result.size());
        Token token = result.get(0);
        assertEquals(1, token.getTokenId());
        assertEquals("token123", token.getToken());
        assertEquals(userId, token.getUserId());

        verify(resultSet, times(1)).getInt("token_id");
        verify(resultSet, times(1)).getString("token");
        verify(resultSet, times(1)).getInt("user_id");
    }

    @Test
    void validTokenGivenGetByTokenReturnsToken() throws Exception {
        String tokenValue = "some-token";
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("token_id")).thenReturn(1);
        when(rs.getString("token")).thenReturn(tokenValue);
        when(rs.getInt("user_id")).thenReturn(123);

        when(jdbcTemplate.query(eq("SELECT * FROM tokens WHERE token = '" + tokenValue + "'"), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Optional<Token>> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });

        Optional<Token> result = tokenDAO.getByToken(tokenValue);

        assertTrue(result.isPresent());
        result.ifPresent(token -> {
            assertEquals(1, token.getTokenId());
            assertEquals(tokenValue, token.getToken());
            assertEquals(123, token.getUserId());
        });
    }

    @Test
    void nonExistingTokenGivenGetByTokenReturnsEmpty() throws Exception {
        String tokenValue = "non-existing-token";
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        when(jdbcTemplate.query(eq("SELECT * FROM tokens WHERE token = '" + tokenValue + "'"), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<Optional<Token>> extractor = invocation.getArgument(1);
                    return extractor.extractData(rs);
                });

        Optional<Token> result = tokenDAO.getByToken(tokenValue);

        assertTrue(result.isEmpty());
    }

    @Test
    void tokenGivenAddExecutesCorrectSqlWithUpdateArguments() {
        Token token = new Token(1, "token123", 1);

        tokenDAO.add(token);

        verify(jdbcTemplate).update(
                eq("INSERT INTO tokens (token, user_id) VALUES (?, ?)"),
                eq(token.getToken()), eq(token.getUserId())
        );
    }

    @Test
    void tokenValueGivenDeleteExecutesCorrectSqlWithUpdateArguments() {
        String tokenValue = "token123";

        tokenDAO.delete(tokenValue);

        verify(jdbcTemplate).update(
                eq("DELETE FROM tokens WHERE token = ?"),
                eq(tokenValue)
        );
    }

}
