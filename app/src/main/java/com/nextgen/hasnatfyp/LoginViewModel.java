package com.nextgen.hasnatfyp;

import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {

    private UserRepository userRepository;

    public LoginViewModel() {
        userRepository = new UserRepository();
    }

    public void login(String username, String password, UserRepository.OnLoginListener listener) {
        userRepository.login(username, password, listener);
    }
}
