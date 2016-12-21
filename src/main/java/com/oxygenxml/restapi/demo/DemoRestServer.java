package com.oxygenxml.restapi.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import ro.sync.ecss.extensions.api.webapp.plugin.WebappServletPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.options.WSOptionsStorage;
import ro.sync.util.URLUtil;

public class DemoRestServer extends WebappServletPluginExtension {

  String restRepositoryURL = "http://rest-repository/";

  /**
   * REST end-points.
   */
  private static final String FILES = "files";
  private static final String FOLDERS = "folders";
  private static final String INFO = "info";
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Common filtering aplied to all requests.
    String docURL = URLUtil.decodeURIComponent(request.getParameter("url"));
    // if no URL is passed or it is not have the URL prefix that we expect.
    if(docURL == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } else if(!docURL.startsWith(restRepositoryURL)) {
      IOUtils.write("The document URL should have the " + restRepositoryURL + " base.", response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    } else {
      WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
      String repoFolderPath = optionsStorage.getOption(DemoRestServerConfigExtension.REPO_FOLDER_PATH, null);
      if(repoFolderPath == null || repoFolderPath.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String message = "Unconfigured REST API Demo Implementation repository folder path.\n"
            + "Configure it on the REST API Demo Implementation Plugin configuration page from the Administration Page.";
        IOUtils.write(message, response.getOutputStream());
        return;
      } 
    }
    
    // the request is Ok, pass it to the methods to handle it.
    super.service(request, response);
  }
  

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String docURL = URLUtil.decodeURIComponent(request.getParameter("url"));
    File docFile = getDocFile(docURL);
    if(!docFile.exists()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String restEndpoint = getRestEndpoint(request);
    switch(restEndpoint) {
    case FILES:
      if(docFile.isDirectory()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      // return the corresponding file.
      IOUtils.copy(new FileInputStream(docFile), response.getOutputStream());

      break;
    case FOLDERS:
      if(!docFile.isDirectory()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      StringBuilder serializedFiles = new StringBuilder("[");

      File[] files = docFile.listFiles();
      for(int i = 0; i < files.length; i++) {
        File file = files[i];
        serializedFiles.append("{ \"name\": \"").append(file.getName()).append("\",")
        .append(" \"folder\": ").append(file.isDirectory()).append(" }")
        // add comma between the array elements.
        .append(i < files.length - 1 ? "," : "");
      }
      serializedFiles.append("]");
      
      IOUtils.write(serializedFiles.toString(), response.getOutputStream());

      break;
    case INFO:
      StringBuilder fileInfo = new StringBuilder();
      fileInfo
      .append("{\"type\": \"").append(docFile.isDirectory() ? "COLLECTION" : "FILE").append("\",")
      .append("\"rootUrl\": \"rest-").append(restRepositoryURL).append("\"")
      .append("}");

      IOUtils.write(fileInfo.toString(), response.getOutputStream());

      break;
    default:
      // no recognized rest endpoint was accessed.
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      break;
    }
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpResponse response = (HttpResponse) resp;

    String restEndpoint = getRestEndpoint(req);
    String docURL = URLUtil.decodeURIComponent(req.getParameter("url"));
    File docFile = getDocFile(docURL);

    switch(restEndpoint) {
    case FILES:

      if(docFile.isDirectory()) {
        response.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      // return the corresponding file.
      IOUtils.copy(req.getInputStream(), new FileOutputStream(docFile));

      break;
    }
  }

  /**
   * Computes the path of the document relative to the repository folder.
   * 
   * @param docURL
   *          the document url sent by the REST Connector.
   * 
   * @return the path of the document relative to the repository folder.
   */
  private File getDocFile(String docURL) throws FileNotFoundException {
    String relativePath = docURL.substring(restRepositoryURL.length());

    File repoFolder = getRepoFolder();
    return new File(repoFolder, relativePath);
  }

  /**
   * @return the configured repository folder.
   */
  private File getRepoFolder() {
    WSOptionsStorage optionsStorage = PluginWorkspaceProvider.getPluginWorkspace().getOptionsStorage();
    String repoFolderPath = optionsStorage.getOption(DemoRestServerConfigExtension.REPO_FOLDER_PATH, null);
    return new File(repoFolderPath);
  }

  
  public static void main(String[] args) {
    String gigi = null;
    new File(gigi);
  }
  
  /**
   * Computes the rest end-point.
   * 
   * @param req  the request.
   * 
   * @return the rest endpoint.
   */
  private String getRestEndpoint(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();

    String[] pathParams = pathInfo.split("/");
    String restEndpoint = pathParams[pathParams.length - 1];


    return restEndpoint;
  }

  @Override
  public String getPath() {
    return "demo-rest-server";
  }
}
