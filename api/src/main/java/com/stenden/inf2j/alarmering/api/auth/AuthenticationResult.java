package com.stenden.inf2j.alarmering.api.auth;

public interface AuthenticationResult<T> {

    static <U> AuthenticationResult<U> success(U user){
        return new Success<U>(user);
    }

    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> incorrectCredentials(){
        return (AuthenticationResult<U>) new IncorrectCredentials();
    }

    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> insufficientPermissions(){
        return (AuthenticationResult<U>) new InsufficientPermissions();
    }

    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> twoFactorAuthRequired(){
        return (AuthenticationResult<U>) new TwoFactorAuthRequired();
    }

    @SuppressWarnings("unchecked")
    static <U> AuthenticationResult<U> userDoesNotExist(){
        return (AuthenticationResult<U>) new UserDoesNotExist();
    }


    final class Success<U> implements AuthenticationResult<U> {

        private final U user;

        private Success(U user) {
            this.user = user;
        }

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

    final class IncorrectCredentials implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.IncorrectCredentials{}";
        }
    }

    final class InsufficientPermissions implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.InsufficientPermissions{}";
        }
    }

    final class TwoFactorAuthRequired implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.TwoFactorAuthRequired{}";
        }
    }

    final class UserDoesNotExist implements AuthenticationResult<Void> {

        @Override
        public String toString() {
            return "AuthenticationResult.UserDoesNotExist{}";
        }
    }
}
