package com.stenden.inf2j.alarmering.api.auth;

/**
 * Result for authentication
 * @param <T> The type of user object that will be returned
 */
public interface AuthenticationResult<T> {

    /**
     * The authentication succeeded
     * @param user The user to return
     * @param <U> The user type that will be returned
     * @return An {@link AuthenticationResult.Success} object
     */
    static <U> AuthenticationResult<U> success(U user){
        return new Success<U>(user);
    }

    /**
     * The credentials like username or password are wrong
     * @param <U> The user type that will be returned
     * @return An {@link AuthenticationResult.IncorrectCredentials} object
     */
    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> incorrectCredentials(){
        return (AuthenticationResult<U>) new IncorrectCredentials();
    }

    /**
     * The user does not have sufficent permissions to log in
     * @param <U> The user type that will be returned
     * @return An {@link AuthenticationResult.InsufficientPermissions} object
     */
    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> insufficientPermissions(){
        return (AuthenticationResult<U>) new InsufficientPermissions();
    }

    /**
     * Additional two-factor authentication is required
     * @param <U> The user type that will be returned
     * @return An {@link AuthenticationResult.TwoFactorAuthRequired} object
     */
    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> twoFactorAuthRequired(){
        return (AuthenticationResult<U>) new TwoFactorAuthRequired();
    }

    /**
     * The user that belongs to the specified username does not exist
     * @param <U> The user type that will be returned
     * @return An {@link AuthenticationResult.UserDoesNotExist} object
     */
    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> userDoesNotExist(){
        return (AuthenticationResult<U>) new UserDoesNotExist();
    }

    /**
     * Class that represents an succeeded authentication attempt
     * @param <U> The user type that will be returned
     */
    final class Success<U> implements AuthenticationResult<U> {

        private final U user;

        private Success(U user) {
            this.user = user;
        }

        /**
         * @return The user that authenticated
         */
        public U getUser() {
            return user;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("AuthenticationResult.Success{");
            sb.append("user=").append(user);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Class that represents the case that the credentials are incorrect
     */
    final class IncorrectCredentials implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.IncorrectCredentials{}";
        }
    }

    /**
     * Class that represents the case that the user does not have sufficient permissions
     */
    final class InsufficientPermissions implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.InsufficientPermissions{}";
        }
    }

    /**
     * Class that represents the case that two factor authentication is required
     */
    final class TwoFactorAuthRequired implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.TwoFactorAuthRequired{}";
        }
    }

    /**
     * Class that represents the case that the user does not exist
     */
    final class UserDoesNotExist implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.UserDoesNotExist{}";
        }
    }
}
