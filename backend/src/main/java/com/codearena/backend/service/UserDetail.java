package com.codearena.backend.service;

import com.codearena.backend.dto.UserDetailsDTO;

public interface UserDetail {

    /**
     * Update the current logged-in user's details.
     *
     * @param dto Updated details
     * @return Updated user details
     */
    UserDetailsDTO updateCurrentUser(UserDetailsDTO dto);

    /**
     * Fetch details of the currently authenticated user.
     *
     * @return Current user's details
     */
    UserDetailsDTO getCurrentUserDetails();

    /**
     * Fetch details of any user using their username.
     *
     * @param email target username
     * @return user details of given username
     */
    UserDetailsDTO getUserDetailsByUsername(String email);
}
