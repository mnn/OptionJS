enablePlugins(ScalaJSPlugin)

name := "OptionJS"

version := "0.0.1"

scalaVersion := "2.11.8"

scalaJSOutputWrapper :=("", "tk.monnef.optionjs.OptionJsInit().main();")

libraryDependencies += "com.lihaoyi" %%% "utest" % "0.4.3" % "test"

testFrameworks += new TestFramework("utest.runner.Framework")

scalaJSUseRhino in Global := false
