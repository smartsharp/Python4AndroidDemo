/*
 * Copyright (C) 2015 Shimoda <kuri65536@hotmail.com>
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.pythoncommon;

import android.content.Context;
import android.content.SharedPreferences;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;
import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class PythonDescriptor extends Sl4aHostedInterpreter {

  protected static final String PYTHON_BIN = "bin/python";
  private static final int LATEST_VERSION_FAILED = -1;
  private int cache_version = -1;
  private int cache_extras_version = -1;
  private int cache_scripts_version = -1;
  private SharedPreferences mPreferences;
  public boolean mfLocalInstall = false;

  // path in devices.
    protected String pathBin() {
        return "/" + PYTHON_BIN;
    }

    protected String pathEgg() {
        return this.pathShlib() + "/python2.7/egg-info";
    }

    protected String pathShlib() {
        return "/lib";
    }

    protected String pathDynload() {
        return this.pathShlib() + "/python2.7/lib-dynload";
    }

    protected String pathSitepkgs() {
        return this.pathShlib() + "/python2.7/site-packages";
    }

    // Network
    protected String urlSrc() {
        throw new UnsupportedOperationException("not implemented in sub-class.");
        // return "http://where.is.python.hosted/path/to/sources/";
    };

    protected String pathVersion() {
        return "python-build/LATEST_VERSION";
    };

    // Script settings.
  public String getExtension() {
    return ".py";
  }

  public String getName() {
    return "python";
  }

  public String getNiceName() {
    return "Python 2.7.10";
  }

  public boolean hasInterpreterArchive() {
    return true;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  protected int __resolve_version(String what) {
    // try resolving latest version
    URL url;
    try {
      url = new URL(this.urlSrc() + this.pathVersion() +
                    what.toUpperCase());
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      return Integer.parseInt(reader.readLine().substring(1).trim());
    } catch (UnknownHostException e) {
      Log.i("can't resolve host: " + this.urlSrc());
      return LATEST_VERSION_FAILED;
    } catch (Exception e) {
      e.printStackTrace();
      return LATEST_VERSION_FAILED;
    }
  }

  public int getVersion() {
    if (cache_version > -1) {
      return cache_version;
    }
    return getVersion(false);
  }

  public int getVersion(boolean usecache) {
    if (usecache && cache_version > -1) {
      return cache_version;
    }
    cache_version = __resolve_version("");
    return cache_version;
  }

  @Override
  public int getExtrasVersion() {
    if (cache_extras_version > -1) {
      return cache_extras_version;
    }
    return getExtrasVersion(false);
  }

  public int getExtrasVersion(boolean usecache) {
    if (usecache && cache_extras_version > -1) {
      return cache_extras_version;
    }
    cache_extras_version = __resolve_version("_extra");
    return cache_extras_version;
  }

  @Override
  public int getScriptsVersion() {
    if (cache_scripts_version > -1) {
      return cache_scripts_version;
    }
    return getScriptsVersion(false);
  }

  public int getScriptsVersion(boolean usecache) {
    if (usecache && cache_scripts_version > -1) {
      return cache_scripts_version;
    }
    cache_scripts_version = __resolve_version("_scripts");
    return cache_scripts_version;
  }

  @Override
  public File getBinary(Context context) {
    return new File(getExtrasPath(context), PYTHON_BIN);
  }

  protected String getExtrasRoot() {
    return InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName()
        + InterpreterConstants.INTERPRETER_EXTRAS_ROOT;
  }

  protected String getHome(Context context) {
    File file = InterpreterUtils.getInterpreterRoot(context, getName());
    return file.getAbsolutePath();
  }

  public String getExtras() {
    File file = new File(getExtrasRoot(), getName());
    return file.getAbsolutePath();
  }

  protected String getTemp() {
    File tmp = new File(getExtrasRoot(), getName() + "/tmp");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    return tmp.getAbsolutePath();
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context context) {
        throw new UnsupportedOperationException("not implemented in sub-class.");
  }

  public void setSharedPreferences(SharedPreferences prefs) {
    mPreferences = prefs;
    cache_version = prefs.getInt(PythonConstants.AVAIL_VERSION_KEY, -1);
    cache_extras_version = prefs.getInt(PythonConstants.AVAIL_EXTRAS_KEY, -1);
    cache_scripts_version =
            prefs.getInt(PythonConstants.AVAIL_SCRIPTS_KEY, -1);
  }

  public boolean isLocalInstall() {
    return this.mfLocalInstall;
  }
}
