import com.jcraft.jsch.*;

/*
File Transfer using STFP via Java program
Create By Humayun Kabir
Developed On 20.02.2020
*/

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static Logger logger = Logger.getLogger(Main.class.getName());
    static boolean isFileFound = false;
    public static void main(String[] args) {
        Session session = null;
        ChannelSftp channelSftp = null;
        //Convention of running this program
        //arg[0] -> host
        //arg[1] -> user
        //arg[2] -> pass
        //arg[3] -> application path
        //arg[4] -> ftp path
        //arg[5] -> copyFromApplication(CFA) or CopyFromFTP(CFF) [3 digit max]
        //arg[6] -> Operation Command [FA - for all files, FP - matching file name using convention, FS- copy all the files]
        //arg[7] -> fileNameConvention [File convention or Full file name]
        try {

//            args = new String[8];
//            args[0] = "127.0.0.1";
//            args[1] = "test_user";
//            args[2] = "test";
//            args[3] = "E:\\test\\";
//            args[4] = "inbound/";
//            args[5] = "CFF";
//            args[6] = "FS";
//            args[7] = "MSRP-MARS00013.txt";
//

            //Initializing variables from arguments
            String host = args[0];
            String user = args[1];
            String pass = args[2];
            String appDir = args[3];
            String ftpDir = args[4];
            String ops = args[5];
            String operationCommand = args[6];
            String fileNameConvention = "";
            if (args.length < 7 || args.length > 8){
                logger.info("Parameters Length should be 7 or 8");
            throw  new Exception();
            }
            if (args.length == 8){
               fileNameConvention = args[7];
            }

            session = createSession(host, user, pass);
            channelSftp = createChannel(session);
            if (ops.equalsIgnoreCase("CFA")) {
                Stream<Path> walk = Files.walk(Paths.get(appDir));
                List<String> result = walk.filter(Files::isRegularFile)
                        .map(x -> x.getFileName().toString()).collect(Collectors.toList());
                for (int i = 0; i < result.size(); i++) {
                        fileMovement(operationCommand, ops, fileNameConvention, channelSftp, appDir + result.get(i), ftpDir + result.get(i));
                }
            } else if (ops.equalsIgnoreCase("CFF")) {
                Vector fileList = channelSftp.ls(ftpDir);
                for (int i = 0; i < fileList.size(); i++) {
                    ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) fileList.get(i);
                    if (!(lsEntry.getFilename().equalsIgnoreCase(".") || lsEntry.getFilename().equalsIgnoreCase(".."))) {
                              fileMovement(operationCommand, ops, fileNameConvention, channelSftp, appDir + lsEntry.getFilename(), ftpDir + lsEntry.getFilename());

                    }
                }
            } else {
                logger.info("Only CFA or CFF is accepted as a OPS parameters ");
            }
            channelSftp.disconnect();
            session.disconnect();
            if (!isFileFound) {
                logger.info("No Such file found");
            }
            logger.info("Connection is disconnected with host - " + host);
        } catch (Exception e) {
            if (session.isConnected()) {
                channelSftp.disconnect();
                session.disconnect();
                logger.info("The connection is terminated due to the error");
            } else {
                logger.info("An Error occur while creating the connection");
            }
            e.printStackTrace();
        }
    }

    private static void fileMovement(String operationCommand, String ops, String fileNameConvention, ChannelSftp channelSftp, String applicationFilePath, String ftpFileAbsolutePath) throws SftpException {
        // Fetch all the files from application server to FTP and vice versa
        if (operationCommand.equalsIgnoreCase("FA")) {
            isFileFound = true;

            //Copy files from Application Server to FTP
            if (ops.equalsIgnoreCase("CFA")) {
                CopyFileFromApplicationServer(channelSftp, applicationFilePath, ftpFileAbsolutePath);
            }
            //Copy files from FTP to Application Server
            else {
                getFilesFromFTP(channelSftp, applicationFilePath, ftpFileAbsolutePath);
            }
        }
        //Fetch all the files matching by the file name from application server to FTP and vice versa
        else if (operationCommand.equalsIgnoreCase("FP")) {

            //Copy files from Application Server to FTP
            if (ops.equalsIgnoreCase("CFA")) {
                String[] processFile = applicationFilePath.split("\\\\");
                if (fileNameConvention.equalsIgnoreCase(processFile[processFile.length - 1].substring(0, fileNameConvention.length()))) {
                    isFileFound = true;
                    CopyFileFromApplicationServer(channelSftp, applicationFilePath, ftpFileAbsolutePath);
                }
            } else {
                //Copy files from FTP to Application Server
                String[] processFile = ftpFileAbsolutePath.split("/");
                if (fileNameConvention.equalsIgnoreCase(processFile[processFile.length - 1].substring(0, fileNameConvention.length()))) {
                    isFileFound = true;
                    getFilesFromFTP(channelSftp, applicationFilePath, ftpFileAbsolutePath);
                }
            }
        }
        //Fetch file matching full file name from Application to FTP and vice versa
        else if (operationCommand.equalsIgnoreCase("FS")) {
            if (ops.equalsIgnoreCase("CFA")) {
                //Copy files from Application Server to FTP
                String[] processFile = applicationFilePath.split("\\\\");
                if (fileNameConvention.equalsIgnoreCase(processFile[processFile.length - 1])) {
                    isFileFound = true;
                    CopyFileFromApplicationServer(channelSftp, applicationFilePath, ftpFileAbsolutePath);
                }
            } else {
                //Copy files from FTP to Application Server
                String[] processFile = ftpFileAbsolutePath.split("/");
                if (fileNameConvention.equalsIgnoreCase(processFile[processFile.length - 1])) {
                    isFileFound = true;
                    getFilesFromFTP(channelSftp, applicationFilePath, ftpFileAbsolutePath);
                }
            }
        } else {
            logger.info("Allowed Operational Movement are FA, FP & FS");
        }
    }

    //Getting Files from FTP to Application Server
    private static void getFilesFromFTP(ChannelSftp channelSftp, String appDir, String ftpDir) throws SftpException {
        logger.info("Fetching Started for " + ftpDir);
        channelSftp.get(ftpDir, appDir);
        logger.info("Fetching complete for " + ftpDir);
        channelSftp.rm(ftpDir);
        logger.info("Delete performed for " + ftpDir);

    }

    //Copying files from Application server to FTP
    private static void CopyFileFromApplicationServer(ChannelSftp channelSftp, String appDir, String ftpDir) throws SftpException {
        channelSftp.put(appDir, ftpDir);
        File file = new File(appDir);
        if (file.delete()) {
            logger.info(appDir + " has been copied and deleted");
        } else {
            logger.info(appDir + " failed to copy and delete");
        }
    }
//Creating Session
    private static Session createSession(String host, String user, String pass) throws JSchException {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host);
        session.setPassword(pass);
        session.setConfig(config);
        session.connect();
        if (session.isConnected()) {
            logger.info("Connection is Created with host - " + host);
        } else {
            logger.info("Connection failed to Create with host - " + host);
        }
        return session;
    }

    private static ChannelSftp createChannel(Session session) throws JSchException {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        return channelSftp;
    }

}

