package ld4s

enum LDError(message: String) extends Exception(message):
  case ClientInitializationError
      extends LDError("failed to initialize LaunchDarkly client")
