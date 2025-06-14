package org.example.ticket.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticket.member.model.Member;
import org.example.ticket.member.repository.MemberRepository;
import org.example.ticket.member.signature.model.dto.SignatureVerifyRequest;
import org.example.ticket.member.signature.service.SignatureService;
import org.example.ticket.security.util.MetamaskUserDetails;
import org.example.ticket.security.token.MetamaskAuthenticationToken;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetamaskAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final SignatureService signatureService;
    private final MemberRepository repository;
    @Override
    public void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        MetamaskAuthenticationToken token = (MetamaskAuthenticationToken) authentication;
        MetamaskUserDetails metamaskUserDetails = (MetamaskUserDetails) userDetails;

        SignatureVerifyRequest request =
                initSignatureVerifyRequest(authentication, token, metamaskUserDetails);

        log.info("add : {} \n sig : {} \n mes : {}", request.getWalletAddress(), request.getSignature(), request.getMessage());

        if (!isSignatureValid(request)) {
            log.info("request : {}", request);
            throw new BadCredentialsException("Signature is not valid");
        }

        log.info("request : {}", request);
    }

    private static SignatureVerifyRequest initSignatureVerifyRequest(UsernamePasswordAuthenticationToken authentication, MetamaskAuthenticationToken token, MetamaskUserDetails metamaskUserDetails) {
        return SignatureVerifyRequest.builder()
                .walletAddress(token.getAddress())
                .signature(authentication.getCredentials().toString())
                .message(String.valueOf(metamaskUserDetails.getNonce()))
                .build();
    }

    @Override
    public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        MetamaskAuthenticationToken auth = (MetamaskAuthenticationToken) authentication;
        Optional<Member> byWalletAddress = repository.findByWalletAddress(auth.getAddress());

        MetamaskUserDetails walletAddress1 = fetchUsersData(byWalletAddress, auth, authorities);

        if (walletAddress1 != null) {
            log.info("나와야 하는 로그 : {}", walletAddress1);
            return walletAddress1;
        }

        log.info("나오면 안되는 로그 : {}", (Object) null);
        throw new BadCredentialsException("address is NULL !");
    }

    @Nullable
    private static MetamaskUserDetails fetchUsersData(Optional<Member> byWalletAddress, MetamaskAuthenticationToken auth, Collection<GrantedAuthority> authorities) {

        Integer nonce;
        String signature;
        String walletAddress;
        String role;

        if(byWalletAddress.isPresent()) {

            walletAddress = auth.getAddress();
            signature = auth.getSignature();
            nonce = byWalletAddress.get().getNonce();
            role = byWalletAddress.get().getRole();

            authorities.add(new SimpleGrantedAuthority(role));
            return new MetamaskUserDetails(walletAddress, signature, nonce, authorities);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(MetamaskAuthenticationToken.class);
    }

    public boolean isSignatureValid(SignatureVerifyRequest request) {
        // Compose the message with nonce
        String message = "Signing a message to login: %s".formatted(request.getMessage());

        // Extract the ‘r’, ‘s’ and ‘v’ components
        byte[] signatureBytes = Numeric.hexStringToByteArray(request.getSignature());
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }
        byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
        Sign.SignatureData data = new Sign.SignatureData(v, r, s);

        // Retrieve public key
        BigInteger publicKey;
        try {
            publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), data);
        } catch (SignatureException e) {
            logger.debug("Failed to recover public key", e);
            return false;
        }

        // Get recovered address and compare with the initial address
        String recoveredAddress = "0x" + Keys.getAddress(publicKey);
        return request.getWalletAddress().equalsIgnoreCase(recoveredAddress);
    }
}
