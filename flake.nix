{
  description = "PurrHolograms — hologram framework (Paper 1.21+, Kotlin, PurrCore + PurrItems)";
  nixConfig = {
    sandbox = "relaxed";
  };

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachSystem [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" ] (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
        };
        jdk = pkgs.jdk21;
        gradle = pkgs.gradle_8;
        ideaPkg = if pkgs.jetbrains ? idea then pkgs.jetbrains.idea else pkgs.jetbrains.idea-community;
        nixFmt = pkgs.nixfmt;
        ktFmtCheck = pkgs.ktlint;
      in
      {
        devShells.default = pkgs.mkShell {
          name = "purrholograms";
          buildInputs = [
            jdk
            gradle
            pkgs.git
            pkgs.bash
            ideaPkg
            nixFmt
            ktFmtCheck
          ];

          shellHook = ''
            export JAVA_HOME=${jdk}
            echo "PurrHolograms — java $(java -version 2>&1 | head -n1) | gradle $(gradle --version | grep Gradle)"
            echo "  gradle test             → unit tests"
            echo "  gradle shadowJar        → build/libs/PurrHolograms-0.1.0-alpha.jar"
          '';
        };

        packages.default = pkgs.stdenv.mkDerivation {
          pname = "PurrHolograms";
          version = "0.1.0-alpha";
          src = ./.;
          nativeBuildInputs = [
            jdk
            gradle
            pkgs.cacert
          ];
          __noChroot = true;
          buildPhase = ''
            export GRADLE_USER_HOME=$TMPDIR/.gradle
            export HOME=$TMPDIR
            gradle --no-daemon -x test build
          '';
          installPhase = ''
            mkdir -p $out
            cp build/libs/*.jar $out/ 2>/dev/null || cp -r build $out/
          '';
        };

        packages.idea = ideaPkg;

        formatter = nixFmt;
      }
    );
}
