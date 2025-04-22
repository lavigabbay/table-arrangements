package com.lavi.tablearrangments.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lavi.tablearrangments.config.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

/**
 * Entity representing a user in the system.
 * <p>
 * Stores login information, personal details, email, status flags, and the authorities (roles) assigned to the user.
 * </p>
 */
@Entity
@Table(name = "jhi_user")
public class User extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key (ID) of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Login name (username). Must match the {@link Constants#LOGIN_REGEX}.
     */
    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    /**
     * Encrypted password. Hidden from API responses.
     */
    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    /**
     * First name of the user.
     */
    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * Last name of the user.
     */
    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * Email address of the user.
     */
    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    /**
     * Whether the user is activated.
     */
    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    /**
     * Language key (e.g., "en", "he") for the user interface.
     */
    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    /**
     * URL to the profile image.
     */
    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    /**
     * Activation key for email-based activation. Hidden from API.
     */
    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    /**
     * Reset key used for password reset. Hidden from API.
     */
    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    /**
     * Date when password reset was requested.
     */
    @Column(name = "reset_date")
    private Instant resetDate = null;

    /**
     * Authorities (roles) granted to the user.
     */
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    /**
     * Gets the user's ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the login name.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login name and converts it to lowercase.
     */
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    /**
     * Gets the password hash.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password hash.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the image URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns whether the user is activated.
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets the user's activation status.
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Gets the activation key.
     */
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * Sets the activation key.
     */
    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    /**
     * Gets the reset key.
     */
    public String getResetKey() {
        return resetKey;
    }

    /**
     * Sets the reset key.
     */
    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    /**
     * Gets the reset date.
     */
    public Instant getResetDate() {
        return resetDate;
    }

    /**
     * Sets the reset date.
     */
    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    /**
     * Gets the language key.
     */
    public String getLangKey() {
        return langKey;
    }

    /**
     * Sets the language key.
     */
    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    /**
     * Gets the authorities.
     */
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    /**
     * Sets the authorities.
     */
    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    /**
     * Equality based on ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    /**
     * Hashcode based on class type.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Returns string representation of the user (excluding password and authorities).
     */
    @Override
    public String toString() {
        return (
            "User{" +
            "login='" +
            login +
            '\'' +
            ", firstName='" +
            firstName +
            '\'' +
            ", lastName='" +
            lastName +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", imageUrl='" +
            imageUrl +
            '\'' +
            ", activated='" +
            activated +
            '\'' +
            ", langKey='" +
            langKey +
            '\'' +
            ", activationKey='" +
            activationKey +
            '\'' +
            "}"
        );
    }
}
