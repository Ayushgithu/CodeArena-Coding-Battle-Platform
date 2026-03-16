package com.codearena.backend.service;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;
import com.codearena.backend.exception.EmailSendingException;

public interface RoomInvitationService {

    /**
     * Send room invitation to a specific email
     */
    void sendRoomInvitation(RoomInvitationDTO invitationDTO) throws EmailSendingException;

    /**
     * Send room invitation to multiple emails
     */
    void sendBulkRoomInvitations(int roomCode, String[] recipientEmails, String inviterName) throws EmailSendingException;

    /**
     * Send invitation to all friends/contacts of the current user
     */
    void sendInvitationToFriends(int roomCode) throws EmailSendingException;
}