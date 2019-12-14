import re # Regular Expressions
import nltk # Natural Language Tool Kit
from nltk import ne_chunk, pos_tag, word_tokenize
from nltk.tree import Tree
# Download some exisitng NLP models from NLTK
nltk.download('punkt')
nltk.download('averaged_perceptron_tagger')
nltk.download('maxent_ne_chunker')
nltk.download('words')


####################################################
################# Meeting Setter ###################
####################################################
class MeetingSetter:
        def __init__(self): # Main Constructor
                # Initialize constants
                self.emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"
                self.durationKeywords = ['h', 'hr', 'hrs', 'hour', 'hours', 'm', 'min', 'mins', 'minutes', 's', 'sec', 'secs', 'seconds']

        def setText(self, text): # Set data extraction source
                self.text = text
        
        def getEmails(self, text):
                # Return all emails found in sorce based on provided Regular Expression for emails 
                #(99% proven capability) - Source: https://emailregex.com/
                return re.findall(self.emailRegex, sentence)

        def getLocations(self, text):
                # Uses NLP to categorize chunks
                # Chunks with tag of LOCATION or ORGANIZATION
                # Words with a preceding 'to'

                # Flag all emails in source before extraction
                allEmailInText = self.getEmails(text)
                for email in allEmailInText:
                        text = text.replace(email, 'IGNORE')

                chunked = ne_chunk(pos_tag(word_tokenize(text)))
                organizations = []
                j = 0
                for i in chunked:
                        if 'ORGANIZATION' in str(i) or 'LOCATION' in str(i):
                                loc = " ".join([token for token, pos in i.leaves()])
                                if loc != "IGNORE": # If not email
                                        organizations.append(loc)
                        else:
                                if j > 0: # If not first word
                                        if 'TO' in str(chunked[j - 1]): # Were proceeded by 'to'
                                                if str(i[0]) != "IGNORE": # If not an email
                                                        organizations.append(i[0])
                        j += 1
                return organizations

        def removeDigitsFromStr(self, text):
                # Utility function to remove all digits from a string, leaving only letters
                return ''.join([i for i in text if not i.isdigit()])

        def getDuration(self, text):
                # Extract duration based on provided general duration specifiers
                duration = ''
                words = text.split(' ')
                for i in range(0, len(words)):
                        if words[i].lower() in self.durationKeywords: # Separate specifier
                                duration += str(words[i - 1]) + str(words[i]) + ' '
                        elif self.removeDigitsFromStr(words[i].lower()) in self.durationKeywords: # Combined specifier
                                duration += str(words[i]) + ' '
                
                return duration

        # Display function to show all extracted data in console
        def display(self):
                emails = self.getEmails(self.text)
                locations = self.getLocations(self.text)
                duration = self.getDuration(self.text)

                print()
                print('Sentence: ' + self.text)
                print('Emails: ' + str(', '.join(emails)))
                print('Locations: ' + str(', '.join(locations)))
                print('Duration: ' + str(duration))
                print()

####################################################
#################### Class End #####################
####################################################

##################### TESTING ######################
# 16 Test Messages for testing Meeting Setter Class
testMessages = [
        'Set up a 3h meeting between talha@mars.com, matt@gmail.com and sheh@yahoo.com at XYZ',
        'Invite abc@yahoo.com  and tt@jck.com to XYZ',
        'Set a meeting with ali@yahoo.com next week',
        'Invite abc@abc.com, abc@zyz.com, abc@ees.com, abc@y.com for a 5 hr brunch',
        'Set up a meeting between mansoor@yahoo.com and matt@gmail.com',
        'set a meeting of 2 hrs between talha@mars.com, matt@gmail.com and sheh@yahoo.com at Oxford Cafe',
        'Let xyz@zyx.com and adc@adc.com know about the 2 hours meeting at NIC',
        'Set a meeting between shehriyarshariq@gmail.com and sshariq.bscs18seecs@seecs.edu.pk at SEECS',
        'Set a reminder for a 2 hrs meeting at 5:30pm between abc@gmail.com and xyz@gmail.com at XYZ',
        'Invite ali@gmail.com to XYZ for a 1 hour 30 mins meeting',
        'Invite someone@outlook.com to NUST for a 30m meeting',
        'Send a reminder to abc@gmail.com for the meeting at 5:30pm today',
        'set a 3hr meeting with abc@email.com',
        'set a meeting at XYZ with abc@gmail.com',
        'Invite abc@abc.com to XYZ',
        'Send an email to zyx@gmail.com for a 1hr 30min meeting at NSTP'
]

meetingSetter = MeetingSetter() # Initialize class
for sentence in testMessages: # For every test message
        meetingSetter.setText(sentence) # Init source for extraction
        meetingSetter.display() # Display extracted data
        
