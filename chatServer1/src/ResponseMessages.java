/**
 * class for response messages
 */
public class ResponseMessages {

    // 1. Establish connection
    final String userNotFound = "User not found";
    final String userAlreadyLoggedIn = "User already logged in";
    final String usernameInvalidFormat = "Username has an invalid format (only characters, numbers and underscores are allowed)";
    final String welcome = "Welcome to TT";
    final String usernameTaken = "The username already exists";

    // logins
    final String loggedInAs = "Logged in as";
    final String usernameTooShort = "You should submit username with more than 2 letters";

    // 4. Terminate connection
    final String goodbye = "Goodbye";

    // 5. Invalid message
    final String invalidMessage = "This is an invalid message ";

    // 7. Private Message
    final String targetUserNotFound = "Target user not found";

    // 8. Create group
    final String groupCreated = "group created";
    final String groupAlreadyExist = "group already exist";
    final String shouldbeMoreThanTwoLetters = "should be more than 2 letters";

    // 9. List groups
    final String noneOfGroup = "None of group";
    final String notInGroup = "You are not the member of the group";

    // 10. Join group
    final String enteredGroup = "Entered group";
    final String groupDoesNotExist = "Group does not exist";
    final String memberInGroup = "You are already the member of the requested group.";
    final String banned = "You cant join this group because you are banned haha.";

    // 12. Private Message
    final String leftGroup = "Left group";

    // 13. Vote kick out
    final String votedToKick = "Voted to kick";
    final String votingCreated = "Voting is created";
    final String targetNotInGroup = "The target user doesn't exist in the group";

    // 14. File transfer
    final String fileTransferReq = "File transfer request arrived.";
    final String reqAccepted = "File transfer request accepted.";
    final String reqRejected = "File transfer request rejected.";
    final String fileSuccess = "File transferred";
    final String fileNotExist = "File does not exist!";
    // other
    final String success = "success";
    final String disconnect = "Client disconnection notice: ";
    final String clientError = "Client error. Connection closed.";
    final String pongTimeout = "Pong timeout. Server disconnects the client.";
    final String togglePingPong = "Ping pong toggled";

}
