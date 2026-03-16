package com.codearena.backend.service;

import com.codearena.backend.dto.RoomInvitationDTO;
import com.codearena.backend.entity.Room;
import com.codearena.backend.entity.User;

public interface EmailTemplateService {
    String createRoomInvitationHtml(Room room, User inviter, RoomInvitationDTO invitationDTO, String invitationLink);
}