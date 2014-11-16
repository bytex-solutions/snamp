module.exports = function(grunt) {
    grunt.initConfig({
        // ----- Environment
        // read in some metadata from project descriptor
        project: grunt.file.readJSON('package.json'),
        // define some directories to be used during build
        dir: {
            // location where TypeScript source files are located
            "source_ts": "ts",
            // location where all build files shall be placed
            "target": "src/main/resources/js",
            // location to place (compiled) javascript files
            "target_js": "src/main/resources/js",
            // location to place documentation, etc.
            "target_report": "js/report"
        },
        // ----- TypeScript compilation
        //  See https://npmjs.org/package/grunt-typescript
        typescript: {
            // Compiles the code into a single file. Also generates a typescript declaration file
            compile: {
                base_path: 'ts',
                src: ['<%= dir.source_ts %>/*.ts'],
                dest: '<%= dir.target_js %>/<%= project.name %>.js',
                options: {
                    basePath: 'ts',
                    target: 'es5',
                    declaration: false,
                    comments: false
                }
            }
        }
    });
    // ----- Setup tasks
    grunt.loadNpmTasks('grunt-typescript');
    grunt.registerTask('default', ['typescript:compile']);
};