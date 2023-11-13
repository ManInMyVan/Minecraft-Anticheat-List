input=input('Input (seperate items with "\\"):\n  ')
input = input.split('\\')

def key(arg):
    list=[]
    for letter in str(arg):
        letter = letter.lower()
        list.append(letter)
    return list
sorted_input=sorted(input, key = key)

print('\nSorted List:')
for item in sorted_input:
    print(' ',item)