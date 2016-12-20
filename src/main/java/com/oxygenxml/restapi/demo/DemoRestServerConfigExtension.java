package com.oxygenxml.restapi.demo;

import ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension;

public class DemoRestServerConfigExtension  extends PluginConfigExtension {

  final static String REPO_FOLDER_PATH = "demo-rest.repo_folder_path";
  
  /**
   * @see ro.sync.ecss.extensions.api.webapp.plugin.PluginConfigExtension#getOptionsForm()
   */
  @Override
  public String getOptionsForm() {
    String repoFolderPath = getRepoFolderPath();
    
    StringBuilder restServerOptions = new StringBuilder()
      .append("<div style='font-family:robotolight, Arial, Helvetica, sans-serif;font-size:0.85em;font-weight: lighter'>")
      .append("<label style='display: block; margin-top: 50px;' >")
      .append("Repository Folder Path: <input  name = '" + REPO_FOLDER_PATH + "' value='" + repoFolderPath + "' ")
      .append("style='width: 320px; line-height: 20px; border: 1px solid #777C7F; background-color: #f7f7f7; border-radius: 5px;' ")
      .append("></input></label>")
      .append("</div>");
    
    return restServerOptions.toString();
  }

  @Override
  public String getPath() {
    return "demo-rest-server-config";
  }

  @Override
  public String getOptionsJson() {
    return "{}";
  }

  /**
   * Fetches the rest server URL from the options.
   * 
   * @return the rest server base url.
   */
  private String getRepoFolderPath() {
    return getOption(REPO_FOLDER_PATH, "");
  }
}