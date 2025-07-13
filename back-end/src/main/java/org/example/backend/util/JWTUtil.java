package org.example.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.backend.entity.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Flyinsky
 * @email w2084151024@gmail.com
 * @date 2024/9/24 14:08
 */
public class JWTUtil {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    /**
     * 密钥
     */
    private static final String SECRET = "45459";

    /**
     * 过期时间
     **/
    private static final long EXPIRATION = 60000000;//单位为秒

    /**
     * 生成用户token,设置token超时时间
     */
    public static String createToken(User user) {
        //过期时间
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 7);
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        String token = JWT.create()
                .withHeader(map)// 添加头部
                //可以将基本信息放到claims中
                .withClaim("id", user.getUserId())//userId
                .withClaim("username", user.getUsername())//username
                .withClaim("password","secret")//password
//                .withClaim("avator",user.getAvator())
                .withExpiresAt(expireDate) //超时设置,设置过期的日期
                .withIssuedAt(new Date()) //签发时间
                .sign(Algorithm.HMAC256(SECRET)); //SECRET加密
        return token;
    }

    /**
     * 校验token并解析token
     */
    public static Map<String, Claim> verifyToken(String token) {
        DecodedJWT jwt = null;
        try {
            if (token == null || token.isEmpty()) {
                logger.error("token为空");
                return null;
            }
            
            logger.info("开始验证token: " + token.substring(0, Math.min(10, token.length())) + "...");
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token);
            logger.info("token验证成功");

            //decodedJWT.getClaim("属性").asString()  获取负载中的属性值

        } catch (Exception e) {
            logger.error("token解码异常: " + e.getMessage());
            logger.error("异常类型: " + e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("根本原因: " + e.getCause().getMessage());
            }
            //解码异常则抛出异常
            return null;
        }
        return jwt.getClaims();
    }
}