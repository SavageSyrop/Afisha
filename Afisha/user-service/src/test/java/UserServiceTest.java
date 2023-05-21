import io.grpc.internal.testing.StreamRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.it.lab.AuthenticateAndGet;
import ru.it.lab.ChangeUserRequest;
import ru.it.lab.Empty;
import ru.it.lab.Id;
import ru.it.lab.Info;
import ru.it.lab.ResetPasswordRequest;
import ru.it.lab.SupportRequestsStream;
import ru.it.lab.UserProto;
import ru.it.lab.dao.AbstractDao;
import ru.it.lab.dao.impl.RoleDaoImpl;
import ru.it.lab.dao.impl.SupportRequestDaoImpl;
import ru.it.lab.dao.impl.UserDaoImpl;
import ru.it.lab.entities.AbstractEntity;
import ru.it.lab.entities.Role;
import ru.it.lab.entities.SupportRequest;
import ru.it.lab.entities.User;
import ru.it.lab.enums.GenderType;
import ru.it.lab.enums.RoleType;
import ru.it.lab.service.MailService;
import ru.it.lab.service.UserServerService;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Random.class)
public class UserServiceTest {
    public Map<Long, User> userMap = new HashMap<>();
    private Long userId = 1L;
    public Map<Long, Role> roleMap = new HashMap<>();
    public Map<Long, SupportRequest> supportRequestMap = new HashMap<>();
    private Long supportRequestId = 1L;

    @Mock
    private UserDaoImpl userDao;

    @Mock
    private RoleDaoImpl roleDao;

    @Mock
    private SupportRequestDaoImpl supportRequestDao;

    @Mock
    private MailService mailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserServerService userService;

    @BeforeEach
    public void setup() {
        setupRoleDao();
        setupUserDao();
        setupSupportRequestDao();
        fillRoleTable();
        fillUserTable();
        fillSupportRequestTable();
    }

    @Test
    void getLoginData_whenUserDontExist_thenThrowsEntityNotFoundException() {
        UserProto request = UserProto.newBuilder()
                .setUsername("test")
                .build();
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        assertThrows(EntityNotFoundException.class, () -> userService.getLoginData(request, responseObserver));
    }

    @Test
    void getLoginData_whenUserExists_thenSuccess() {
        String searchedUsername = "desertfox";
        UserProto request = UserProto.newBuilder()
                .setUsername(searchedUsername)
                .build();
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        userService.getLoginData(request, responseObserver);
        assertNull(responseObserver.getError());
        List<UserProto> responseList = responseObserver.getValues();
        assertEquals(1, responseList.size());
        UserProto response = responseList.get(0);
        assertEquals(searchedUsername, response.getUsername());
    }

    @Test
    public void registerUser_whenInvalidEmail_thenThrowsIllegalArgumentException() {
        UserProto request = UserProto.newBuilder()
                .setUsername("TestUser")
                .setPassword("testPassword0")
                .setDateOfBirth(new Date(6, Calendar.NOVEMBER, 2010).getTime())
                .setGenderType(GenderType.FEMALE.name())
                .setEmail("invalidMail")
                .setRoleId(2)
                .setIsOpenProfile(true).build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.registerUser(request, responseObserver);
        assertNotNull(responseObserver.getError());
    }

    @Test
    public void registerUser_whenInvalidUsername_thenThrowsIllegalArgumentException() {
        UserProto request = UserProto.newBuilder()
                .setUsername("test")
                .setPassword("testPassword0")
                .setDateOfBirth(new Date(6, Calendar.NOVEMBER, 2010).getTime())
                .setGenderType(GenderType.FEMALE.name())
                .setEmail("fess.2002@mail.ru")
                .setRoleId(2)
                .setIsOpenProfile(true).build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.registerUser(request, responseObserver);
        assertNotNull(responseObserver.getError());
    }

    @Test
    public void registerUser_whenUserAlreadyExists_thenThrowsStatusRuntimeException() {
        String searchedUser = "desertfox";
        assertEquals(userDao.getByUsername(searchedUser).getUsername(), searchedUser);
        UserProto request = UserProto.newBuilder()
                .setUsername(searchedUser)
                .setPassword("testPassword0")
                .setDateOfBirth(new Date(6, Calendar.NOVEMBER, 2010).getTime())
                .setGenderType(GenderType.FEMALE.name())
                .setEmail("fess.2002@mail.ru")
                .setRoleId(2)
                .setIsOpenProfile(true).build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.registerUser(request, responseObserver);
        assertNotNull(responseObserver.getError());
    }

    @Test
    public void registerUser_whenUserDontExist_thenSuccess() {
        String addedUserName = "NewUserName54";
        assertNull(userDao.getByUsername(addedUserName));
        UserProto request = UserProto.newBuilder()
                .setUsername(addedUserName)
                .setPassword("testPassword0")
                .setDateOfBirth(new Date(6, Calendar.NOVEMBER, 2010).getTime())
                .setGenderType(GenderType.FEMALE.name())
                .setEmail("fess.2002@mail.ru")
                .setRoleId(2)
                .setIsOpenProfile(true).build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.registerUser(request, responseObserver);
        assertNull(responseObserver.getError());
        List<Info> listResponse = responseObserver.getValues();
        assertEquals(1L, listResponse.size());
    }


    @Test
    public void changeUserData_whenNewUsernameInvalid_thenThrowsStatusRuntimeException() {
        String invalidUsername = "test";
        String oldUsername = "desertfox";
        ChangeUserRequest request = ChangeUserRequest.newBuilder()
                .setOldUsername(oldUsername)
                .setNewUsername(invalidUsername).build();
        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        userService.changeUserData(request, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("INVALID_ARGUMENT"));
    }

    @Test
    public void changeUserData_whenNewEmailInvalid_thenThrowsStatusRuntimeException() {
        String validUsername = "UserTest";
        String oldUsername = "desertfox";
        String invalidEmail = "invalid";
        ChangeUserRequest request = ChangeUserRequest.newBuilder()
                .setOldUsername(oldUsername)
                .setNewUsername(validUsername)
                .setEmail(invalidEmail)
                .build();
        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        userService.changeUserData(request, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("INVALID_ARGUMENT"));
    }

    @Test
    public void changeUserData_whenNewUsernameAlreadyExists_thenThrowsStatusRuntimeException() {
        String alreadyExistingUsername = "aleoonka";
        String oldUsername = "desertfox";
        String validEmail = "validMail@mail.ru";
        ChangeUserRequest request = ChangeUserRequest.newBuilder()
                .setOldUsername(oldUsername)
                .setNewUsername(alreadyExistingUsername)
                .setEmail(validEmail)
                .build();
        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        userService.changeUserData(request, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("ALREADY_EXISTS"));
    }

    @Test
    public void changeUserData_whenNewDataCorrect_thenSuccess() {
        String newUsername = "UserTest";
        String oldUsername = "desertfox";
        String validEmail = "validMail@mail.ru";
        ChangeUserRequest request = ChangeUserRequest.newBuilder()
                .setOldUsername(oldUsername)
                .setNewUsername(newUsername)
                .setEmail(validEmail)
                .build();
        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        userService.changeUserData(request, responseObserver);
        assertNull(responseObserver.getError());
        assertNotNull(userDao.getByUsername(newUsername));
    }


    @Test
    public void changePassword_whenDataCorrect_thenSuccess() {
        String username = "desertfox";
        String newPassword = "testpassword";
        assertNotEquals(userDao.getByUsername(username).getPassword(), newPassword);
        UserProto request = UserProto.newBuilder()
                .setUsername(username)
                .setPassword(newPassword)    // тестируется обновление, а не валидность, поэтому шифровки нет
                .build();
        StreamRecorder<Empty> responseObserver = StreamRecorder.create();
        userService.changePassword(request, responseObserver);
        assertNull(responseObserver.getError());
        assertEquals(userDao.getByUsername(username).getPassword(), newPassword);
    }


    @Test
    public void getPrivacy_whenDataIsCorrect_thenSuccess() {
        String username = "desertfox";
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        UserProto request = UserProto.newBuilder()
                .setUsername(username)
                .build();
        userService.getPrivacy(request, responseObserver);
        assertNull(responseObserver.getError());
        assertEquals(1, responseObserver.getValues().size());
    }

    @Test
    public void togglePrivacy_whenDataCorrect_thenSuccess() {
        String username = "desertfox";
        User user = userDao.getByUsername(username);
        Boolean privacyBefore = user.getIsOpenProfile();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        UserProto request = UserProto.newBuilder()
                .setUsername(username)
                .build();
        userService.togglePrivacy(request, responseObserver);
        assertNull(responseObserver.getError());
        user = userDao.getByUsername(username);
        Boolean privacyAfter = user.getIsOpenProfile();
        assertNotEquals(privacyBefore, privacyAfter);
    }


    @Test
    public void getUserByUsername_whenUserWithEnteredUsernameDontExist_thenThrowsStatusRuntimeException() {
        String username = "abobus";
        User user = userDao.getByUsername(username);
        assertNull(user);
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        userService.getUserByUsername(UserProto.newBuilder().setUsername(username).build(), responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("NOT_FOUND"));
    }


    @Test
    public void getUserByUsername_whenUserWithEnteredUsernameExists_thenSuccess() {
        String username = "desertfox";
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        userService.getUserByUsername(UserProto.newBuilder().setUsername(username).build(), responseObserver);
        assertEquals(responseObserver.getValues().get(0).getUsername(), username);
    }

    @Test
    public void getUserById_whenUserWithSuchIdDontExist_thenThrowsEntityNotFoundException() {
        long nonExistingId = 54L;
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(Id.newBuilder().setId(nonExistingId).build(), StreamRecorder.create()));
    }

    @Test
    public void getUserById_whenUserWithSuchIdExists_thenSuccess() {
        long existingId = 1L;
        StreamRecorder<UserProto> responseObserver = StreamRecorder.create();
        userService.getUserById(Id.newBuilder().setId(existingId).build(), responseObserver);
        assertEquals(responseObserver.getValues().get(0).getId(), existingId);
    }


    @Test
    public void activateAccount_whenInvalidCode_thenThrowsStatusRuntimeException() {
        Info invalid_code = Info.newBuilder().setInfo("invalid_code").build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.activateAccount(invalid_code, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("NOT_FOUND"));
    }

    @Test
    public void activateAccount_whenValidCode_thenSuccess() {
        String activationCode = "valid_code";
        Info code = Info.newBuilder().setInfo(activationCode).build();
        User user = userDao.getById(1L);
        user.setActivationCode(activationCode);
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.activateAccount(code, responseObserver);
        assertNull(responseObserver.getError());
    }

    @Test
    public void resetPassword_whenInvalidCode_thenThrowsStatusRuntimeException() {
        String resetCode = "invalid_code";
        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setCode(resetCode)
                .setNewPassword("Suspass0")
                .build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.resetPassword(request, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("NOT_FOUND"));
    }

    @Test
    public void resetPassword_whenValidCode_thenSuccess() {
        String resetCode = "valid_code";
        User user = userDao.getById(1L);
        user.setRestorePasswordCode(resetCode);

        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setCode(resetCode)
                .setNewPassword("correctPassword5")
                .build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.resetPassword(request, responseObserver);
        assertNull(responseObserver.getError());
        assertNull(user.getActivationCode());
    }

    @Test
    public void forgotPassword_whenUserWithEnteredUsernameDontExist_thenThrowsStatusRuntimeException() {
        String username = "vaflya";
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.forgotPassword(Info.newBuilder().setInfo(username).build(), responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("NOT_FOUND"));
    }

    @Test
    public void forgotPassword_whenUserWithEnteredUsernameExists_thenSuccess() {
        String username = "desertfox";
        User user = userDao.getByUsername(username);
        assertNull(user.getRestorePasswordCode());
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.forgotPassword(Info.newBuilder().setInfo(username).build(), responseObserver);
        assertNull(responseObserver.getError());
        assertNotNull(user.getRestorePasswordCode());

    }

    @Test
    public void requestRole_whenDataCorrect_thenSuccess() {
        UserProto user = UserProto.newBuilder()
                .setUsername("aleoonka")
                .setRoleId(3)
                .build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.requestRole(user, responseObserver);
        assertNull(responseObserver.getError());
    }

    @Test
    public void setRole_whenDataCorrect_thenSuccess() {
        String username = "aleoonka";
        User user = userDao.getByUsername(username);
        Long prevRoleId = user.getRole().getId();
        assertEquals(prevRoleId, user.getRole().getId());
        Long settedRoleId = 3L;
        UserProto userRequest = UserProto.newBuilder()
                .setUsername(username)
                .setRoleId(settedRoleId)
                .build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.setRole(userRequest, responseObserser);
        assertEquals(settedRoleId, user.getRole().getId());
    }

    @Test
    public void banUser_whenTryingToBanAdmin_thenThrowsStatusRuntimeException() {
        Long adminId = 1L;
        UserProto req = UserProto.newBuilder().setId(adminId).build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.banUser(req, responseObserser);
        assertTrue(responseObserser.getError().getMessage().contains("CANCELLED"));
    }

    @Test
    public void banUser_whenUserIsAlreadyBanned_thenThrowsStatusRuntimeException() {
        Long bannedUserId = 2L;
        User banndedUser = userMap.get(bannedUserId);
        banndedUser.setIsBanned(true);
        userMap.put(bannedUserId, banndedUser);
        UserProto req = UserProto.newBuilder().setId(bannedUserId).build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.banUser(req, responseObserser);
        assertTrue(responseObserser.getError().getMessage().contains("CANCELLED"));
    }

    @Test
    public void banUser_whenDataCorrect_thenSuccess() {
        Long userToBanId = 2L;
        User userToBan = userMap.get(userToBanId);
        assertFalse(userToBan.getIsBanned());
        UserProto req = UserProto.newBuilder().setId(userToBanId).build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.banUser(req, responseObserser);
        assertTrue(userToBan.getIsBanned());
    }

    @Test
    public void unbanUser_whenUserIsNotBanned_thenThrowsStatusRuntimeException() {
        Long notBannedUserId = 2L;
        User notBannedUser = userMap.get(notBannedUserId);
        assertFalse(notBannedUser.getIsBanned());
        UserProto req = UserProto.newBuilder().setId(notBannedUserId).build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.unbanUser(req, responseObserser);
        assertTrue(responseObserser.getError().getMessage().contains("CANCELLED"));
    }

    @Test
    public void unbanUser_whenDataCorrect_thenSuccess() {
        Long bannedUserId = 2L;
        User bannedUser = userMap.get(bannedUserId);
        bannedUser.setIsBanned(true);
        userMap.put(bannedUserId, bannedUser);
        UserProto req = UserProto.newBuilder().setId(bannedUserId).build();
        StreamRecorder<Info> responseObserser = StreamRecorder.create();
        userService.unbanUser(req, responseObserser);
        assertFalse(bannedUser.getIsBanned());
    }

    @Test
    public void getSupportRequestsByUsername_whenDataCorrect_thenSuccess() {
        String existingUsername = supportRequestMap.get(1L).getUser().getUsername();
        AuthenticateAndGet request = AuthenticateAndGet.newBuilder().setUsername(existingUsername).build();
        StreamRecorder<SupportRequestsStream> responseObserver = StreamRecorder.create();
        userService.getSupportRequestsByUsername(request,responseObserver);
        assertTrue(responseObserver.getValues().get(0).getRequestsList().size()>0);
        assertNull(responseObserver.getError());
    }

    @Test
    public void getSupportRequestById_whenTryingToAccessOtherPlayerRequest_thenThrowsStatusRuntimeException() {
        String wrongUsername = "desertfox";
        AuthenticateAndGet request = AuthenticateAndGet.newBuilder()
                .setUsername(wrongUsername)
                .setSearchedId(1L)
                .build();
        StreamRecorder<ru.it.lab.SupportRequest> responseObserver = StreamRecorder.create();
        userService.getSupportRequestById(request,responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("UNAUTHENTICATED"));
    }

    @Test
    public void getSupportRequestById_whenDataCorrect_thenSuccess() {
        String authorUsername = "aleoonka";
        Long searchedId = 1L;
        AuthenticateAndGet request = AuthenticateAndGet.newBuilder()
                .setUsername(authorUsername)
                .setSearchedId(searchedId)
                .build();
        StreamRecorder<ru.it.lab.SupportRequest> responseObserver = StreamRecorder.create();
        userService.getSupportRequestById(request,responseObserver);
        assertNull(responseObserver.getError());
        assertEquals(responseObserver.getValues().get(0).getId(), searchedId);

    }

    @Test
    public void createSupportRequest_whenDataCorrect_thenSuccess() {
        String question = "Test question";
        String username = "aleoonka";
        ru.it.lab.SupportRequest supportRequest = ru.it.lab.SupportRequest.newBuilder()
                .setUsername(username)
                .setQuestion(question)
                .build();
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        userService.createSupportRequest(supportRequest,responseObserver);
        assertNull(responseObserver.getError());
        assertEquals(supportRequestMap.get(supportRequestId-1).getUser().getUsername(), username);
        assertEquals(supportRequestMap.get(supportRequestId-1).getQuestion(), question);

    }

    @Test
    public void closeSupportRequest_whenRequestIsAlreadyClosed_thenThrowsStatusRuntimeException() {
        String adminUsername = "desertfox";
        SupportRequest supportRequest = supportRequestMap.get(2L);
        assertNull(supportRequest.getAnswer());
        supportRequest.setAnswer("answer");
        supportRequest.setAdmin(userDao.getByUsername(adminUsername));
        supportRequest.setCloseTime(LocalDateTime.now());
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        ru.it.lab.SupportRequest req = ru.it.lab.SupportRequest.newBuilder()
                .setId(supportRequest.getId())
                .setUsername(adminUsername)
                .setAnswer("answer")
                .build();
        userService.closeSupportRequest(req, responseObserver);
        assertTrue(responseObserver.getError().getMessage().contains("ALREADY_EXISTS"));
    }

    @Test
    public void closeSupportRequest_whenDataCorrect_thenSuccess() {
        String adminUsername = "desertfox";
        SupportRequest supportRequest = supportRequestMap.get(2L);
        assertNull(supportRequest.getCloseTime());
        StreamRecorder<Info> responseObserver = StreamRecorder.create();
        ru.it.lab.SupportRequest req = ru.it.lab.SupportRequest.newBuilder()
                .setId(supportRequest.getId())
                .setUsername(adminUsername)
                .setAnswer("answer")
                .build();
        userService.closeSupportRequest(req, responseObserver);
        assertNotNull(supportRequest.getCloseTime());
    }

    @Test
    public void getAllOpenSupportRequests_whenDataIsCorrect_thenSuccess() {
        StreamRecorder<SupportRequestsStream> responseObserver = StreamRecorder.create();
        userService.getAllOpenSupportRequests(Empty.newBuilder().build(), responseObserver);
        assertNull(responseObserver.getError());
        assertNotNull(responseObserver.getValues());
    }


    private <T extends AbstractEntity> void setupGetById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                return hashmap.get(id);
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).getById(anyLong());
    }

    private <T extends AbstractEntity> void setupDeleteById(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            Long id = invocationOnMock.getArgument(0);
            if (hashmap.containsKey(id)) {
                hashmap.remove(id);
                return null;
            }
            String[] splittedClassName = className.getName().split("\\.");
            throw new EntityNotFoundException(splittedClassName[splittedClassName.length - 1] + " with id " + id + " is not found!");
        }).when(dao).deleteById(anyLong());
    }

    private <T extends AbstractEntity> void setupCreate(Map<Long, T> hashmap, AbstractDao<T> dao, Class<T> className) {
        lenient().doAnswer(invocationOnMock -> {
            T entity = invocationOnMock.getArgument(0);
            Long id = (long) hashmap.size();
            while (hashmap.containsKey(id)) {
                id++;
            }
            entity.setId(id);
            hashmap.put(id, entity);
            return entity;
        }).when(dao).create(any(className));
    }

    private void setupSupportRequestDao() {
        lenient().doAnswer(invocationOnMock -> {
            SupportRequest supportRequest = invocationOnMock.getArgument(0);
            supportRequest.setId(supportRequestId);
            supportRequestMap.put(supportRequestId, supportRequest);
            supportRequestId++;
            return supportRequest;
        }).when(supportRequestDao).create(any(SupportRequest.class));

        setupDeleteById(supportRequestMap, supportRequestDao, SupportRequest.class);
        setupGetById(supportRequestMap, supportRequestDao, SupportRequest.class);
        lenient().doAnswer(invocationOnMock -> {
            List<SupportRequest> res = new ArrayList<>();
            for (SupportRequest supportRequest : supportRequestMap.values()) {
                if (supportRequest.getCloseTime() == null) {
                    res.add(supportRequest);
                }
            }
            return res;
        }).when(supportRequestDao).getAllOpenRequests();

        lenient().doAnswer(invocationOnMock -> {
            String username = invocationOnMock.getArgument(0);
            List<SupportRequest> res = new ArrayList<>();
            for (SupportRequest supportRequest : supportRequestMap.values()) {
                if (supportRequest.getUser().getUsername().equals(username)) {
                    res.add(supportRequest);
                }
            }
            return res;
        }).when(supportRequestDao).getAllByUser(any(String.class));
    }


    private void setupRoleDao() {
        setupGetById(roleMap, roleDao, Role.class);
        lenient().doAnswer(invocationOnMock -> {
            Role role = invocationOnMock.getArgument(0);
            Long id = (long) roleMap.size();
            while (roleMap.containsKey(id)) {
                id++;
            }
            role.setId(id);
            role.setPermissions(new ArrayList<>());
            roleMap.put(id, role);
            return role;
        }).when(roleDao).create(any(Role.class));
        setupDeleteById(roleMap, roleDao, Role.class);
    }

    private void setupUserDao() {
        setupGetById(userMap, userDao, User.class);
        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            User userByUsername = userDao.getByUsername(user.getUsername());
            if (userByUsername != null) {
                throw new RuntimeException("User with " + user.getUsername() + " already exists");
            }
            user.setId(userId);
            userMap.put(userId, user);
            userId++;
            return user;
        }).when(userDao).create(any(User.class));

        setupDeleteById(userMap, userDao, User.class);


        lenient().doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);

            for (User userFromMap : userMap.values()) {
                if (!Objects.equals(user.getId(), userFromMap.getId()) && user.getUsername().equals(userFromMap.getUsername()))
                    throw new RuntimeException("User with " + user.getUsername() + " already exists");
            }

            userMap.put(user.getId(), user);
            return user;
        }).when(userDao).update(any(User.class));

        lenient().doAnswer(invocationOnMock -> {
            String username = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByUsername(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String restoreCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (restoreCode.equals(user.getRestorePasswordCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByResetCode(any(String.class));

        lenient().doAnswer(invocationOnMock -> {
            String activationCode = invocationOnMock.getArgument(0);
            for (User user : userMap.values()) {
                if (activationCode.equals(user.getActivationCode())) {
                    return user;
                }
            }
            return null;
        }).when(userDao).getByActivationCode(any(String.class));
    }

    private void addUser(Long id, String username, String password, String email, Date dateOfBirth, GenderType genderType, Long roleId, Boolean isOpenProfile) {
        Role role = roleMap.get(roleId);
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);
        user.setGenderType(genderType);
        user.setRole(role);
        user.setIsBanned(false);
        user.setIsOpenProfile(isOpenProfile);
        user.setActivationCode(null);
        user.setRestorePasswordCode(null);
        this.userMap.put(id, user);
        userId++;
    }

    private void addSupportRequest(Long id, User user, User admin, String answer, String question, LocalDateTime creationTime, LocalDateTime closeTime) {
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setId(id);
        supportRequest.setUser(user);
        supportRequest.setAnswer(answer);
        supportRequest.setQuestion(question);
        supportRequest.setCloseTime(closeTime);
        supportRequest.setCreationTime(creationTime);
        supportRequest.setAdmin(admin);
        supportRequestMap.put(id, supportRequest);
        supportRequestId++;
    }

    private void fillUserTable() {
        addUser(1L, "desertfox", "$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u", "fess.2002@mail.ru", new Date(9, Calendar.NOVEMBER, 2001), GenderType.MALE, 1L, true);
        addUser(2L, "aleoonka", "$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq", "fess.2002@mail.ru", new Date(23, Calendar.JULY, 2002), GenderType.FEMALE, 2L, true);
    }

    private void fillSupportRequestTable() {
        User user = userMap.get(2L);
        User admin = userMap.get(1L);
        addSupportRequest(1L,user,admin,"Answer","Question",LocalDateTime.now(),LocalDateTime.now());
        addSupportRequest(2L, user,null,null, "Unanswered", LocalDateTime.now(),null);
    }

    private void fillRoleTable() {
        Role admin = new Role();
        admin.setName(RoleType.ADMIN);
        admin.setId(1L);
        admin.setPermissions(new ArrayList<>());
        Role user = new Role();
        user.setName(RoleType.USER);
        user.setId(2L);
        user.setPermissions(new ArrayList<>());
        Role organizer = new Role();
        organizer.setName(RoleType.ORGANIZER);
        organizer.setPermissions(new ArrayList<>());
        organizer.setId(3L);
        roleMap.put(1L, admin);
        roleMap.put(2L, user);
        roleMap.put(3L, organizer);
    }
}
