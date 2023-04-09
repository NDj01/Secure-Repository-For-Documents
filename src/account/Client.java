package account;

import java.util.List;

public class Client {

    private String username;
    private String password;
    private String countryName;
    private String stateOrProvinceName;
    private String localityName;
    private String organizationName;
    private String organizationalUnitName;
    private String emailAddress;
    private String keyPath;

    private String certificatePath;
    private List<String> repositoryFiles;

    public Client(){
        super();
    }
    public Client(String username, String password, String countryName, String stateOrProvinceName, String localityName, String organizationName, String organizationalUnitName, String emailAddress){
        this.username = username;
        this.password = password;
        this.countryName = countryName;
        this.stateOrProvinceName = stateOrProvinceName;
        this.localityName = localityName;
        this.organizationName = organizationName;
        this.organizationalUnitName = organizationalUnitName;
        this.emailAddress = emailAddress;
    }

    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getCountryName(){
        return countryName;
    }
    public String getStateOrProvinceName(){
        return stateOrProvinceName;
    }
    public String getLocalityName(){
        return localityName;
    }
    public String getOrganizationName(){
        return organizationName;
    }
    public String getOrganizationalUnitName(){
        return organizationalUnitName;
    }
    public String getEmailAddress(){
        return emailAddress;
    }
    public String getKeyPath(){
        return keyPath;
    }
    public List<String> getRepositoryFiles(){
        return repositoryFiles;
    }
    public String getCertificatePath(){
        return certificatePath;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setCountryName(String countryName){
        this.countryName = countryName;
    }
    public void setStateOrProvinceName(String stateOrProvinceName){
        this.stateOrProvinceName = stateOrProvinceName;
    }
    public void setLocalityName(String localityName){
        this.localityName = localityName;
    }
    public void setOrganizationName(String organizationName){
        this.organizationName = organizationName;
    }
    public void setOrganizationalUnitName(String organizationalUnitName){
        this.organizationalUnitName = organizationalUnitName;
    }
    public void setEmailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    public void setKeyPath(String keyPath){
        this.keyPath = keyPath;
    }
    public void setCertificatePath(String certificatePath){
        this.certificatePath = certificatePath;
    }
    public void setRepositoryFiles(List<String > repositoryFiles){
        this.repositoryFiles = repositoryFiles;
    }
    @Override
    public String toString(){
        return username + " " + password + " " + countryName + " " + stateOrProvinceName + " " + localityName + " " + organizationName + " " + organizationalUnitName + " " + emailAddress;
    }
}
