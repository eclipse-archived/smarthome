def baseDir = new File("$project.basedir")
def restFolder = new File(baseDir, '/documentation/features/rest/')
restFolder.mkdirs()
def parentFolder = new File(baseDir.getParent())
def ioFolder = new File(parentFolder, '/bundles/io/')
ioFolder.eachFileRecurse(){
	def swaggerFolderName = 'target' + File.separator + 'generated' + File.separator + 'swagger'
	if(it.isFile() && it.getParent().endsWith(swaggerFolderName) && it.getName().endsWith('.md')){
		def copy = new File(restFolder.getPath() + '/' +  it.getName())
		def input = it.newInputStream()
		def output = copy.newOutputStream()

		output << input

		input.close()
		output.close()
	}
}


