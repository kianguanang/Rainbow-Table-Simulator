Requirements

  This program is built using java version 19.0.1

Compilation instruction

  To compile the program, use:

    javac Rainbow.java

  To run the program, use:
    
    java Rainbow <password filename>
    Example: java Rainbow Passwords.txt

Instructions:

  Program will prompt user for a hash input and then display pre-image password if found in list

Reduction Function

  r = MD5(password) % sizeOfPasswordFile
  where
    r - reduction value
    password - the current password to be hashed using MD5 hash functions
    sizeOfPasswordFile - total number of passwords contained in the password file

  Reduction example:

    1. Using password "10th"
    2. The corresponding MD5 hashstring will be "515da2caf582ac4801cbb5d876c73c90"
    3. The sizeOfPasswordFile is 25143 based on the Passwords.txt file provided in the assignment specs
    4. Using BigInteger library, we convert the hashstring into a long value 108153653096848345464776048863879838864
    5. r will be 9878
    6. using r as the index to find the next password in the chain, we get "gobbledygook"
    7. repeat 1 - 6 for 4 more times to form the chain
  
Disclaimer

  No validation is done on the user input.