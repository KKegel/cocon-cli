#!/bin/bash

if [ ! -f "./build/distributions/cocon-cli-0.1-alpha/bin/cocon-cli" ]; then
  # Check if the file ./build/distributions/cocon-cli-0.1-alpha.zip exists
  if [ ! -f "./build/distributions/cocon-cli-0.1-alpha.zip" ]; then
      echo "File ./build/distributions/cocon-cli-0.1-alpha.zip does not exist."
      echo "Running ./gradlew build to create the file."
      # Run the gradlew build command
      ./gradlew build
      # Check if the build was successful
      if [ $? -ne 0 ]; then
          echo "Build failed. Please check the output for errors."
          exit 1
      fi
  fi

  # Check if the file ./build/distributions/cocon-cli-0.1-alpha/bin/cocon-cli exists
  if [ ! -f "./build/distributions/cocon-cli-0.1-alpha/bin/cocon-cli" ]; then
      echo "File ./build/distributions/cocon-cli-0.1-alpha/bin/cocon-cli does not exist."
      echo "Unzipping the file ./build/distributions/cocon-cli-0.1-alpha.zip."
      # Unzip the file
      unzip -o ./build/distributions/cocon-cli-0.1-alpha.zip -d ./build/distributions
      # Check if the unzip was successful
      if [ $? -ne 0 ]; then
          echo "Unzip failed. Please check the output for errors."
          exit 1
      fi
  fi
fi

# Execute the run script with the provided arguments
./build/distributions/cocon-cli-0.1-alpha/bin/cocon-cli "$@"