package com.academicblogfptu.AcademicBlogFPTU.controllers;

import com.academicblogfptu.AcademicBlogFPTU.config.UserAuthProvider;
import com.academicblogfptu.AcademicBlogFPTU.dtos.AdminDtos.ActivitiesLogDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.CommentDtos.ReportedCommentDto;
import com.academicblogfptu.AcademicBlogFPTU.dtos.UserDtos.*;
import com.academicblogfptu.AcademicBlogFPTU.entities.*;
import com.academicblogfptu.AcademicBlogFPTU.exceptions.AppException;
import com.academicblogfptu.AcademicBlogFPTU.repositories.ActivitiesLogRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.PostRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserDetailsRepository;
import com.academicblogfptu.AcademicBlogFPTU.repositories.UserRepository;
import com.academicblogfptu.AcademicBlogFPTU.services.AdminServices;
import com.academicblogfptu.AcademicBlogFPTU.services.NotifyByMailServices;
import com.academicblogfptu.AcademicBlogFPTU.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserManageController {

    private final UserServices userService;
    private final AdminServices adminService;
    private final UserAuthProvider userAuthProvider;
    private final UserDetailsRepository userDetailsRepository;
    private final PostRepository postRepository;
    private final NotifyByMailServices notifyByMailServices;
    private final ActivitiesLogRepository activitiesLogRepository;
    private final UserRepository userRepository;
    public boolean isAdmin(UserDto userDto) {
        return userDto.getRoleName().equals("admin");
    }

    @GetMapping("activities-log")
    public ResponseEntity<List<Map<String, Object>>> viewActivitiesLog(@RequestHeader("Authorization") String headerValue) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<Object[]> actionsAndFullName = activitiesLogRepository.findAllActionsAndFullNames();
            List<Map<String, Object>> responseList = actionsAndFullName.stream().map(entry -> {
                Map<String, Object> responseMap = new LinkedHashMap<>();
                responseMap.put("id", entry[0]);
                responseMap.put("actionTime", entry[1]);
                responseMap.put("action", entry[2]);
                responseMap.put("actor", entry[3]);
                return responseMap;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<HashMap<String, Object>> viewDashboard(@RequestHeader("Authorization") String headerValue) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            try {
                List<UserEntity> userInfos = userRepository.findAllExceptAdmins();
                List<PostEntity> totalPost = postRepository.getAllApprovedPost();
                List<ReportedProfileDto> reportedProfileDto = adminService.viewReportProfile();
                List<ReportedCommentDto> reportCommentList = adminService.viewPendingReportComment();

                // Thực hiện HTTP request bằng HttpURLConnection
                URL url = new URL("https://lvnsoft.store/TotalVisit/visit-count.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    JsonParser jsonParser = JsonParserFactory.getJsonParser();
                    Map<String, Object> jsonData = jsonParser.parseMap(response.toString());
                    HashMap<String, Object> responseMap = new HashMap<>();
                    responseMap.put("total_user", userInfos.size());
                    responseMap.put("total_post", totalPost.size());
                    responseMap.put("total_reported_profile", reportedProfileDto.size());
                    responseMap.put("total_reported_comment", reportCommentList.size());
                    responseMap.put("total_visit", jsonData );

                    return ResponseEntity.ok(responseMap);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<ListUserDto>> getAllUsers(@RequestHeader("Authorization") String headerValue) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            List<Object[]> userInfos = userDetailsRepository.getAllUsersInfo();
            Map<Integer, ListUserDto> userMap = new HashMap<>();

            for (Object[] userInfo : userInfos) {
                if (!isAdmin(userService.findByUsername(userInfo[1].toString()))) {
                    Integer userId = (Integer) userInfo[0];
                    ListUserDto userDetailsInfo = userMap.get(userId);

                    if (userDetailsInfo == null) {
                        userDetailsInfo = new ListUserDto();
                        userDetailsInfo.setId(userId);
                        userDetailsInfo.setUsername(userInfo[1].toString());
                        userDetailsInfo.setPassword(userInfo[2].toString());
                        userDetailsInfo.setFullname(userInfo[3].toString());
                        userDetailsInfo.setEmail(userInfo[4] != null ? userInfo[4].toString() : null);
                        userDetailsInfo.setPhone(userInfo[5] != null ? userInfo[5].toString() : null);
                        userDetailsInfo.setBanned((Boolean) userInfo[6]);
                        userDetailsInfo.setMuted((Boolean) userInfo[7]);
                        userDetailsInfo.setMutetime(userInfo[8] != null ? (Timestamp) userInfo[8] : null);
                        userDetailsInfo.setRole((RoleEntity) userInfo[9]);
                        Object majorObject = userInfo[10];
                        userDetailsInfo.setMajor(majorObject != null ? (MajorEntity) majorObject : null);
                        userMap.put(userId, userDetailsInfo);
                    }

                    Integer skillId = userInfo[11] != null ? (Integer) userInfo[11] : null;
                    String skillName = userInfo[12] != null ? userInfo[12].toString() : null;

                    // Trong vòng lặp
                    if (skillId != null) {
                        SkillEntity skill = new SkillEntity();
                        skill.setId(skillId.intValue());
                        skill.setSkillName(skillName);
                        userDetailsInfo.getSkills().add(skill); // Thêm skill vào danh sách
                    }

                }
            }

            List<ListUserDto> users = new ArrayList<>(userMap.values());
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity<UserDto> RegisterAccount(@RequestHeader("Authorization") String headerValue, @RequestBody RegisterDto registerDto) {
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            if (adminService.isEmailExist(registerDto.getEmail())) throw new AppException("Mail exist" , HttpStatus.UNAUTHORIZED);
            UserDto userDto = adminService.register(registerDto);
            adminService.RegisterUserDetail(registerDto);
            userDto.setFullname(registerDto.getFullname());
            notifyByMailServices.sendRegisterMail(registerDto.getEmail(),registerDto.getUsername(),registerDto.getPassword());
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Tạo tài khoản với ID: " + userDto.getId());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);
            return ResponseEntity.ok(userDto);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/set-role")
    public ResponseEntity<HashMap<String, String>> SetRole(@RequestHeader("Authorization") String headerValue, @RequestBody SetRoleDto setRoleDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            UserDto userDto = adminService.findById(setRoleDto.getId());
            userDto.setRoleName(setRoleDto.getRole());
            UserDto userSet = userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", "")));
            adminService.setRoleUser(userDto,userSet.getId());
            HashMap < String, String > responseMap = new HashMap<>();
            responseMap.put("message", "Set role success.");
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Đã thay đổi role của " + userDto.getFullname() + " thành " + userDto.getRoleName());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto adminDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(adminDto.getId());
            adminService.saveActivity(activitiesLogDto);
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/set-major")
    public ResponseEntity<HashMap<String, String>> SetMajor(@RequestHeader("Authorization") String headerValue, @RequestBody SetMajorDto setMajorDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminService.setMajorUser(setMajorDto.getId(), setMajorDto.getMajorID());
            HashMap < String, String > responseMap = new HashMap<>();
            responseMap.put("message", "Set major success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/ban-user")
    public ResponseEntity<HashMap<String, String>> BanUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminService.banUser(adminService.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Ban success.");
            UserDto getName = adminService.findById(identificationDto.getId());
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Cấm tài khoản " +  getName.getFullname());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto userDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(userDto.getId());
            adminService.saveActivity(activitiesLogDto);
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/unban-user")
    public ResponseEntity<HashMap<String, String>> UnbanUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminService.unbanUser(adminService.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Unban success.");
            UserDto getName = adminService.findById(identificationDto.getId());
            ActivitiesLogDto activitiesLogDto = new ActivitiesLogDto();
            activitiesLogDto.setAction("Gỡ cấm tài khoản " +  getName.getFullname());
            long currentTimeMillis = System.currentTimeMillis();
            Timestamp expirationTimestamp = new Timestamp(currentTimeMillis);
            activitiesLogDto.setActionTime(expirationTimestamp);
            String username = userAuthProvider.getUser(headerValue.replace("Bearer ", ""));
            UserDto userDto = userService.findByUsername(username);
            activitiesLogDto.setUserID(userDto.getId());
            adminService.saveActivity(activitiesLogDto);
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/mute-user")
    public ResponseEntity<HashMap<String, String>> MuteUser(@RequestHeader("Authorization") String headerValue, @RequestBody MuteDto muteDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            Date currentTime = new Date();

            // Tính thời điểm kết thúc dựa trên thời điểm hiện tại và thời lượng cấm (đơn vị là giờ)
            long muteDurationMillis = muteDto.getMuteDuration() * 3600000; // 1 giờ = 3600000 ms
            Date muteEndTime = new Date(currentTime.getTime() + muteDurationMillis);
            Timestamp timestamp = new Timestamp(muteEndTime.getTime());
            adminService.muteUser(adminService.findById(muteDto.getId()), timestamp);
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Mute success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/unmute-user")
    public ResponseEntity<HashMap<String, String>> UnmuteUser(@RequestHeader("Authorization") String headerValue, @RequestBody IdentificationDto identificationDto){
        if (isAdmin(userService.findByUsername(userAuthProvider.getUser(headerValue.replace("Bearer ", ""))))) {
            adminService.unmuteUser(adminService.findById(identificationDto.getId()));
            HashMap <String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Unmute success.");
            return ResponseEntity.ok(responseMap);
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
