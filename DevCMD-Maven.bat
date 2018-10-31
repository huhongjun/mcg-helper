@set AD_HOME=D:\ADeveloper

@rem -----J2EE-----
@set JAVA_HOME=%AD_HOME%\java\jdk1.8.0_77x64
@set PATH=%JAVA_HOME%\bin;%PATH%

@rem -----Maven-----
@set M2_HOME=%AD_HOME%\apache-maven\maven-3.5.4
@set M2=%M2_HOME%\bin
@set PATH=%M2_HOME%\bin;%PATH%
@set MAVEN_OPTS=-Xms256m -Xmx512m

@rem -----Git-----
@set GIT_HOME=D:\ARun\PortableGit
@set PATH=%GIT_HOME%\bin;%PATH%

@rem -----Sublime Text 3---
@set SUBLIME_HOME="C:\Program Files\Sublime Text 3"
@set PATH=%SUBLIME_HOME%;%PATH%;

@%COMSPEC%

