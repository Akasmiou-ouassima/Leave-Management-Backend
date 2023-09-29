package com.nttdata.sid.services;

import com.nttdata.sid.entities.Utilisateur;

public class EmailContentGenerator {
    private static String logoLink = EmailUtils.getLogoLink();
    private static String facebook = EmailUtils.getFacebookLink();
    private static String twitter = EmailUtils.getTwitterLink();
    private static String linkedin = EmailUtils.getLinkedinLink();
    private static String facebookNtt = EmailUtils.getFacebookNTTDATA();
    private static String twitterNtt = EmailUtils.getTwitterNTTDATA();
    private static String linkedinNtt = EmailUtils.getLinkedinNTTDATA();
    private static String nttLink = EmailUtils.getNttLink();
    private static String styles = EmailStyles.getStyles();
    public static  String getEmailContent(String token, Utilisateur utilisateur) {
        return "<html>"
                + styles
                + "<body>"
                + "  <div class='image-container'>"
                + "    <img src='" + logoLink + "' alt='Logo' class='image'/>"
                + "  </div>"
                + "<div class='container'>"
                + "  <div class='text-container'>"
                + "    <b>Hi"+" "+ utilisateur.getPrenom()+" "+utilisateur.getNom()+","+"</b><br>"
                + "    <p>Sorry to hear you’re having trouble logging into <span>RestQuest</span><br><br>"
                + "       We got a message that you forgot your password. If this was you, <br><br>"
                + "       reset your password now.</p><br><br>"
                + "    <div class='button-container' style='text-align: center;'>"
                + "      <a href='http://localhost:4200/reset-password?token=" + token + "' class='button-link'>"
                + "       <span style='color: #fff;'> Reset Password </span>"
                + "      </a>"
                + "    </div>"
                + "    <div class='social-icons'>"
                + "      <a href='"+facebookNtt+"'><img src='" + facebook + "' style='width: 30px; height:30px;margin-right:10px'/></a>"
                + "      <a href='"+twitterNtt+"'><img src='" + twitter + "'  style='width: 28px; height:28px;margin-right:10px''/></a>"
                + "      <a href='"+linkedinNtt+"'><img src='" + linkedin + "' style='width: 30px; height:30px;margin-right:10px''/></a>"
                + "    </div>"
                + "    <div class='ntt'><a href='"+nttLink+"'>© 2023 NTT DATA </a></div>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
    public static String getEmailNewUserContent(String password, Utilisateur utilisateur) {
        return "<html>"
                + styles
                + "<body>"
                + "  <div class='image-container'>"
                + "    <img src='" + logoLink + "' alt='Logo' class='image'/>"
                + "  </div>"
                + "<div class='container'>"
                + "  <div class='text-container'>"
                + "    <b>Hi"+" "+ utilisateur.getPrenom()+" "+utilisateur.getNom()+","+"</b><br>"
                + "    <p>You have been added to our application <span>RestQuest</span> to manage your leave requests efficiently. <br><br>"
                + "      Here is your temporary password, but remember to change it as soon as possible: "
                + password + "<br><br>"
                + "       Please follow this link to access the application :</p><br>"
                + "    <div class='button-container' style='text-align: center;'>"
                + "      <a href='http://localhost:4200' class='button-link'>"
                + "       <span style='color: #fff;'> Access Application </span>"
                + "      </a>"
                + "    </div>"
                + "    <div class='social-icons'>"
                + "      <a href='"+facebookNtt+"'><img src='" + facebook + "' style='width: 30px; height:30px;margin-right:10px'/></a>"
                + "      <a href='"+twitterNtt+"'><img src='" + twitter + "'  style='width: 28px; height:28px;margin-right:10px''/></a>"
                + "      <a href='"+linkedinNtt+"'><img src='" + linkedin + "' style='width: 30px; height:30px;margin-right:10px''/></a>"
                + "    </div>"
                + "    <div class='ntt'><a href='"+nttLink+"'>© 2023 NTT DATA </a></div>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public static String getEmailManagerContent(String msg, Utilisateur manager, Utilisateur employee) {
        return "<html>"
                + styles
                + "<body>"
                + "  <div class='image-container'>"
                + "    <img src='" + logoLink + "' alt='Logo' class='image'/>"
                + "  </div>"
                + "<div class='container'>"
                + "  <div class='text-container'>"
                + "    <b>Hi"+" "+ manager.getPrenom()+" "+manager.getNom()+","+"</b><br>"
                + "    <p>The employee "+employee.getPrenom()+" "+employee.getNom()+" has submitted a leave " + msg + ".<br><br>"
                + "        Please follow this link to review the request :</p><br>"
                + "    <div class='button-container' style='text-align: center;'>"
                + "      <a href='http://localhost:4200/leaves' class='button-link'>"
                + "       <span style='color: #fff;'> Follow Link </span>"
                + "      </a>"
                + "    </div>"
                + "    <div class='social-icons'>"
                + "      <a href='"+facebookNtt+"'><img src='" + facebook + "' style='width: 30px; height:30px;margin-right:10px'/></a>"
                + "      <a href='"+twitterNtt+"'><img src='" + twitter + "'  style='width: 28px; height:28px;margin-right:10px''/></a>"
                + "      <a href='"+linkedinNtt+"'><img src='" + linkedin + "' style='width: 30px; height:30px;margin-right:10px''/></a>"
                + "    </div>"
                + "    <div class='ntt'><a href='"+nttLink+"'>© 2023 NTT DATA </a></div>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    public static String getEmailEmployeeContent(String status,Utilisateur employee) {
        return "<html>"
                + styles
                + "<body>"
                + "  <div class='image-container'>"
                + "    <img src='" + logoLink + "' alt='Logo' class='image'/>"
                + "  </div>"
                + "<div class='container'>"
                + "  <div class='text-container'>"
                + "    <b>Hi"+" "+ employee.getPrenom()+" "+employee.getNom()+","+"</b><br>"
                + "    <p>Your leave request has been " + status + " by your team manager.<br><br>"
                + "       Please follow this link to review the request :</p><br>"
                + "    <div class='button-container' style='text-align: center;'>"
                + "      <a href='http://localhost:4200/Myleaves' class='button-link'>"
                + "       <span style='color: #fff;'> Follow Link </span>"
                + "      </a>"
                + "    </div>"
                + "    <div class='social-icons'>"
                + "      <a href='"+facebookNtt+"'><img src='" + facebook + "' style='width: 30px; height:30px;margin-right:10px'/></a>"
                + "      <a href='"+twitterNtt+"'><img src='" + twitter + "'  style='width: 28px; height:28px;margin-right:10px''/></a>"
                + "      <a href='"+linkedinNtt+"'><img src='" + linkedin + "' style='width: 30px; height:30px;margin-right:10px''/></a>"
                + "    </div>"
                + "    <div class='ntt'><a href='"+nttLink+"'>© 2023 NTT DATA </a></div>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

}
